package ch.ehi.gbdbsvalidator.gui;

import ch.ehi.gbdbsvalidator.Main;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    private javax.swing.JPanel jContentPane = null;
    private javax.swing.JPanel infoPanel = null;
    private javax.swing.JPanel buttonPanel = null;
    private javax.swing.JButton okButton = null;
    private javax.swing.JLabel programVersionLabel = null;
    private javax.swing.JLabel programVersion = null;
    private javax.swing.JLabel javaVersionLabel = null;
    private javax.swing.JLabel javaVmVersionLabel = null;
    private javax.swing.JLabel javaVersion = null;
    private javax.swing.JLabel javaVmVersion = null;


    public AboutDialog(java.awt.Frame owner) {
        super(owner, true);
        initialize();
        getProgramVersion().setText(Main.getVersion());
        getJavaVersion().setText(System.getProperty("java.version"));
        getJavaVmVersion().setText(System.getProperty("java.vm.version"));
        this.pack();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setContentPane(getJContentPane());
        this.setTitle("About " + Main.APP_NAME);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getInfoPanel(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        }
        return jContentPane;
    }

    /**
     * This method initializes infoPanel with all children
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getInfoPanel() {
        if (infoPanel == null) {
            infoPanel = new javax.swing.JPanel();
            infoPanel.setLayout(new java.awt.GridBagLayout());
            
            int cy=0;
            {
                java.awt.GridBagConstraints consGridBagConstraints1 = new java.awt.GridBagConstraints();
                java.awt.GridBagConstraints consGridBagConstraints2 = new java.awt.GridBagConstraints();
                consGridBagConstraints1.gridx = 0;
                consGridBagConstraints1.gridy = cy;
                consGridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints1.fill = GridBagConstraints.BOTH;
                consGridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
                consGridBagConstraints2.gridx = 1;
                consGridBagConstraints2.gridy = cy;
                consGridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints2.fill = GridBagConstraints.BOTH;
                
                infoPanel.add(getProgramVersionLabel(), consGridBagConstraints1);
                infoPanel.add(getProgramVersion(), consGridBagConstraints2);
            }

            cy++;
            {
                java.awt.GridBagConstraints consGridBagConstraints7 = new java.awt.GridBagConstraints();
                java.awt.GridBagConstraints consGridBagConstraints8 = new java.awt.GridBagConstraints();
                consGridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints7.gridx = 0;
                consGridBagConstraints7.gridy = cy;
                consGridBagConstraints7.insets = new java.awt.Insets(0, 0, 5, 12);
                consGridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints8.gridx = 1;
                consGridBagConstraints8.gridy = cy;
                consGridBagConstraints8.insets = new java.awt.Insets(0, 0, 5, 12);

                infoPanel.add(getJavaVersionLabel(), consGridBagConstraints7);
                infoPanel.add(getJavaVersion(), consGridBagConstraints8);
            }

            cy++;
            {
                java.awt.GridBagConstraints consGridBagConstraints9 = new java.awt.GridBagConstraints();
                java.awt.GridBagConstraints consGridBagConstraints10 = new java.awt.GridBagConstraints();
                consGridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints9.gridx = 0;
                consGridBagConstraints9.gridy = cy;
                consGridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
                consGridBagConstraints10.gridx = 1;
                consGridBagConstraints10.gridy = cy;

                infoPanel.add(getJavaVmVersionLabel(), consGridBagConstraints9);
                infoPanel.add(getJavaVmVersion(), consGridBagConstraints10);
            }

        }
        return infoPanel;
    }

    private javax.swing.JLabel getProgramVersionLabel() {
        if (programVersionLabel == null) {
            programVersionLabel = new javax.swing.JLabel();
            programVersionLabel.setText("Program version");
        }
        return programVersionLabel;
    }

    private javax.swing.JLabel getProgramVersion() {
        if (programVersion == null) {
            programVersion = new javax.swing.JLabel();
        }
        return programVersion;
    }


    private javax.swing.JLabel getJavaVersionLabel() {
        if (javaVersionLabel == null) {
            javaVersionLabel = new javax.swing.JLabel();
            javaVersionLabel.setText("Java version");
        }
        return javaVersionLabel;
    }

    private javax.swing.JLabel getJavaVersion() {
        if (javaVersion == null) {
            javaVersion = new javax.swing.JLabel();
        }
        return javaVersion;
    }

    private javax.swing.JLabel getJavaVmVersionLabel() {
        if (javaVmVersionLabel == null) {
            javaVmVersionLabel = new javax.swing.JLabel();
            javaVmVersionLabel.setText("Java-VM version");
        }
        return javaVmVersionLabel;
    }

    private javax.swing.JLabel getJavaVmVersion() {
        if (javaVmVersion == null) {
            javaVmVersion = new javax.swing.JLabel();
        }
        return javaVmVersion;
    }

    private javax.swing.JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new javax.swing.JPanel();
            buttonPanel.add(getOkButton(), null);
        }
        return buttonPanel;
    }

    private javax.swing.JButton getOkButton() {
        if (okButton == null) {
            okButton = new javax.swing.JButton();
            okButton.setText("OK");
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dispose();
                }
            });
        }
        return okButton;
    }

}
