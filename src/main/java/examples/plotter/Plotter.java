// Copyright (c) 2007 by David J. Biesack, All Rights Reserved.
// Author: David J. Biesack David.Biesack@sas.com
// Created on Nov 4, 2007

package examples.plotter;

import javax.swing.*;

/**
 * This is a Swing application which demonstrates the use of
 * javax.tools.Compiler via the Facade class,
 * javaxtools.compiler.CharSequenceCompiler. See {@link PlotterPanel} for more
 * information.
 *
 * @author <a href="mailto:David.Biesack@sas.com">David J. Biesack</a>
 */
final public class Plotter extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Display the plot GUI and allow user interaction.
     *
     * @param args
     */
    public static void main(final String[] args) {
        try {
            new Plotter().setVisible(true);
        } catch (Throwable e) {
            presentException(e);
        }
    }

    static void presentException(Throwable t) {
        String title = "Unable to run the " + Plotter.class.getName() + " application.";
        String message = title
                + " \n"
                + "This may be due to a missing tools.jar or missing JFreeChart jars. \n"
                + "Please consult the docs/README file found with this application for further details.";
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Default constructor for the plotter application
     */
    public Plotter() {
        super("javaxtools.compiler demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(new PlotterPanel());
        pack();
    }

}
