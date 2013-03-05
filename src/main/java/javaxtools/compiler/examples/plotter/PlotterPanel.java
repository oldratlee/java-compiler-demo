// Copyright (c) 2007 by David J. Biesack, All Rights Reserved.
// Author: David J. Biesack David.Biesack@sas.com
// Created on Nov 4, 2007

package javaxtools.compiler.examples.plotter;

import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;
import static javax.swing.SpringLayout.WEST;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javaxtools.compiler.CharSequenceCompiler;
import javaxtools.compiler.CharSequenceCompilerException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This is a Swing JPanel which demonstrates the use of javax.tools.Compiler via
 * the Facade class, javaxtools.compiler.CharSequenceCompiler. See
 * {@link Plotter}.
 * <p>
 * The GUI provides a text field where the user can enter a numeric formula
 * definition for a math function double f(double x). When entered, this class
 * builds a new Java class which implements the Function interface, then
 * compiles that class, loads the class, and creates an instance which is then
 * used to generate a plot. Compiler diagnostics are recorded in a scrollable
 * text area below the plot.
 * 
 * @author <a href="mailto:David.Biesack@sas.com">David J. Biesack</a>
 * 
 */
final public class PlotterPanel extends JPanel {

   private static final long serialVersionUID = 1L;
   // a default, interesting function
   private static final String DEFAULT_FUNCTION = "x * (sin(x) + cos(x))";
   // GUI inter-gadget padding, in pixels
   private static final int PAD = 5;
   // Create a CharSequenceCompiler instance which is used to compile
   // expressions into Java classes which are then used to create the XY plots.
   // The -target 1.5 options are simply an example of how to pass javac
   // compiler
   // options (the generated source in this example is Java 1.5 compatible.)
   private final CharSequenceCompiler<Function> compiler = new CharSequenceCompiler<Function>(
         getClass().getClassLoader(), Arrays.asList(new String[] { "-target", "1.5" }));
   // for unique class names
   private int classNameSuffix = 0;
   // package name; a random number is appended
   private static final String PACKAGE_NAME = "javaxtools.compiler.examples.plotter.runtime";
   // for secure package name
   private static final Random random = new Random();
   // the Java source template
   private String template;

   // The compiled function which runs f(x)
   private Function function;
   // GUI drawing panel
   private final PlotPanel plotPanel = new PlotPanel();
   // scrollable error view
   private final JTextArea errors = new JTextArea();
   // user function/expression input field
   private final JTextField plotFunctionText = new JTextField(DEFAULT_FUNCTION, 40);

   public static void main(final String[] args) {
      new PlotterPanel().setVisible(true);
   }

   /**
    * Constructor for the plotter panel. Creates the visuals and performs layout
    * and attatches event handlers.
    */
   public PlotterPanel() {
      Container c = this;
      SpringLayout layout = new SpringLayout();
      c.setLayout(layout);
      JLabel label = new JLabel("f(x)=");
      JButton plotButton = new JButton("Plot this function");
      c.add(label);
      c.add(plotFunctionText);
      c.add(plotButton);
      ActionListener plot = new ActionListener() {
         public void actionPerformed(ActionEvent action) {
            generateAndPlotFunction();
         }
      };
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent evt) {
            generateAndPlotFunction();
         }
      });
      plotButton.addActionListener(plot);
      plotFunctionText.addActionListener(plot);
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setViewportView(errors);
      add(plotPanel);
      c.add(scrollPane);

      layout.putConstraint(NORTH, label, PAD, NORTH, c);
      layout.putConstraint(NORTH, plotButton, PAD, NORTH, c);
      layout.putConstraint(NORTH, plotFunctionText, PAD, NORTH, c);
      layout.putConstraint(WEST, label, PAD, WEST, c);
      layout.putConstraint(EAST, plotButton, -PAD, EAST, c);
      layout.putConstraint(WEST, plotFunctionText, PAD, EAST, label);
      layout.putConstraint(EAST, plotFunctionText, -PAD, WEST, plotButton);
      layout.putConstraint(EAST, plotPanel, -PAD, EAST, c);
      layout.putConstraint(WEST, plotPanel, PAD, WEST, c);
      layout.putConstraint(NORTH, plotPanel, PAD, SOUTH, plotButton);
      layout.putConstraint(SOUTH, plotPanel, -PAD, NORTH, scrollPane);
      layout.putConstraint(NORTH, scrollPane, PAD, SOUTH, plotPanel);
      layout.putConstraint(EAST, scrollPane, -PAD, EAST, c);
      layout.putConstraint(WEST, scrollPane, PAD, WEST, c);
      layout.putConstraint(SOUTH, scrollPane, -PAD, SOUTH, c);
      layout.putConstraint(NORTH, scrollPane, -40, SOUTH, c);
      setPreferredSize(new Dimension(800, 600));
   }

   /**
    * 
    * Generate Java source from the user function, then create a graph using
    * JFreeChart
    */
   void generateAndPlotFunction() {
      final String source = plotFunctionText.getText();
      function = newFunction(source);
      final XYSeries series = new XYSeries(source);
      for (int i = -100; i <= 100; i++) {
         double x = i / 10.0;
         series.add(x, function.f(x));
      }
      final XYDataset xyDataset = new XYSeriesCollection(series);

      boolean legend = false;
      boolean tooltips = true;
      boolean urls = false;
      JFreeChart chart = ChartFactory.createXYLineChart( //
            "f(x)=" + source, // Title
            "x", // X-Axis label
            "f(x)", // Y-Axis label
            xyDataset, PlotOrientation.VERTICAL, legend, tooltips, urls);
      final BufferedImage image = chart.createBufferedImage(plotPanel.getWidth(),
            plotPanel.getHeight());
      final JLabel plotComponent = new JLabel();
      plotComponent.setIcon(new ImageIcon(image));
      plotPanel.image = image;
      plotPanel.repaint();
   }

   /**
    * A simple panel which contains a scaled image
    */
   static class PlotPanel extends JPanel {
      private static final long serialVersionUID = 1L;
      BufferedImage image;

      @Override
      public void paint(final Graphics g) {
         if (image != null) {
            g.drawImage(image, 0, 0, this);
         } else {
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, getWidth(), getHeight());
         }
      }
   }

   /**
    * Generate Java source for a Function which computes f(x)=expr
    * 
    * @param expr
    *           String representation of Java expression that returns a double
    *           value for an input value x. The class in which this expression
    *           is embedded uses static import for all the members of the
    *           java.lang.Math class so they can be accessed without
    *           qualification.
    * @return an object which computes the function denoted by expr
    */
   Function newFunction(final String expr) {
      errors.setText("");
      try {
         // generate semi-secure unique package and class names
         final String packageName = PACKAGE_NAME + digits();
         final String className = "Fx_" + (classNameSuffix++) + digits();
         final String qName = packageName + '.' + className;
         // generate the source class as String
         final String source = fillTemplate(packageName, className, expr);
         // compile the generated Java source
         final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
         Class<Function> compiledFunction = compiler.compile(qName, source, errs,
               new Class<?>[] { Function.class });
         log(errs);
         return compiledFunction.newInstance();
      } catch (CharSequenceCompilerException e) {
         log(e.getDiagnostics());
      } catch (InstantiationException e) {
         errors.setText(e.getMessage());
      } catch (IllegalAccessException e) {
         errors.setText(e.getMessage());
      } catch (IOException e) {
         errors.setText(e.getMessage());
      }
      return NULL_FUNCTION;
   }

   /**
    * @return random hex digits with a '_' prefix
    */
   private String digits() {
      return '_' + Long.toHexString(random.nextLong());
   }

   /**
    * Return the Plotter function Java source, substituting the given package
    * name, class name, and double expression
    * 
    * @param packageName
    *           a valid Java package name
    * @param className
    *           a valid Java class name
    * @param expression
    *           text for a double expression, using double x
    * @return source for the new class implementing Function interface using the
    *         expression
    * @throws IOException
    */
   private String fillTemplate(String packageName, String className, String expression)
         throws IOException {
      if (template == null)
         template = readTemplate();
      // simplest "template processor":
      String source = template.replace("$packageName", packageName)//
            .replace("$className", className)//
            .replace("$expression", expression);
      return source;
   }

   /**
    * Read the Function source template
    * 
    * @return a source template
    * @throws IOException
    */
   private String readTemplate() throws IOException {
      InputStream is = PlotterPanel.class.getResourceAsStream("Function.java.template");
      int size = is.available();
      byte bytes[] = new byte[size];
      if (size != is.read(bytes, 0, size))
         throw new IOException();
      return new String(bytes, "US-ASCII");
   }

   /**
    * Log diagnostics into the error JTextArea
    * 
    * @param diagnostics
    *           iterable compiler diagnostics
    */
   private void log(final DiagnosticCollector<JavaFileObject> diagnostics) {
      final StringBuilder msgs = new StringBuilder();
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
            .getDiagnostics()) {
         msgs.append(diagnostic.getMessage(null)).append("\n");
      }
      errors.setText(msgs.toString());

   }

   /**
    * Null Object pattern to use when there are exceptions with the function
    * expression.
    */
   static final Function NULL_FUNCTION = new Function() {
      public double f(final double x) {
         return 0.0;
      }
   };

}
