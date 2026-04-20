package org.openpnp.machine.photon.sheets.gui;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.JBindings;
import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MonospacedFontTableCellRenderer;
import org.openpnp.gui.support.RotationCellValue;
import org.openpnp.machine.photon.PhotonFeeder;
import org.openpnp.machine.photon.PhotonFeederSlots;
import org.openpnp.machine.photon.PhotonProperties;
import org.openpnp.model.Configuration;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GlobalConfigConfigurationWizard extends AbstractConfigurationWizard {

    private final PhotonProperties photonProperties;

    private final FeederSearchProgressBar progressBarPanel;
    private final JButton searchButton;
    private final JSpinner maxFeederSpinner;
    private final JButton btnStartFeedSlotsWizard;
    private final JLabel lblNewLabel;
    private final AutoSelectTextTable slotsTable;
    private final PhotonFeederSlotsTableModel slotsTableModel;
    private final JButton moveCameraButton;
    private final JButton captureSlotLocationButton;

    /**
     * Create the panel.
     */
    public GlobalConfigConfigurationWizard() {
        photonProperties = new PhotonProperties(Configuration.get().getMachine());

        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(searchPanel);
        searchPanel.setLayout(new FormLayout(new ColumnSpec[]{
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("50dlu"),
                ColumnSpec.decode("4dlu:grow"),
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,},
                new RowSpec[]{
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("10dlu"),
                        FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel lblMaxFeeder = new JLabel("Maximum Feeder Address To Scan");
        searchPanel.add(lblMaxFeeder, "2, 2");

        int initialMaxFeederAddress = photonProperties.getMaxFeederAddress();
        SpinnerNumberModel maxFeederSpinnerModel = new SpinnerNumberModel(
                initialMaxFeederAddress, 1, 254, 1
        );
        maxFeederSpinner = new JSpinner(maxFeederSpinnerModel);
        searchPanel.add(maxFeederSpinner, "4, 2");

        searchButton = new JButton("Search");
        searchButton.addActionListener(searchAction);
        searchPanel.add(searchButton, "6, 2");

        progressBarPanel = new FeederSearchProgressBar();
        searchPanel.add(progressBarPanel, "2, 4, 5, 1, fill, fill");
        progressBarPanel.setVisible(false);
        progressBarPanel.setNumberOfElements(initialMaxFeederAddress);

        JPanel programFeederSlotsPanel = new JPanel();
        programFeederSlotsPanel.setBorder(new TitledBorder(null, "Program Feeder Slots", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(programFeederSlotsPanel);
        programFeederSlotsPanel.setLayout(new FormLayout(new ColumnSpec[]{
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("4dlu:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,},
                new RowSpec[]{
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        RowSpec.decode("6dlu:grow"),
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,}));

        lblNewLabel = new JLabel("If you've built your own slots and need to program them, use this wizard.");
        programFeederSlotsPanel.add(lblNewLabel, "2, 2, 3, 1");

        btnStartFeedSlotsWizard = new JButton("Start Wizard");
        btnStartFeedSlotsWizard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(! Configuration.get().getMachine().isEnabled()) {
                    UiUtils.showError(new Exception("Please connect to the machine before running this wizard."));
                    return;
                }

                ProgramFeederSlotWizard wizard = new ProgramFeederSlotWizard();
                wizard.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                wizard.setVisible(true);
            }
        });
        programFeederSlotsPanel.add(btnStartFeedSlotsWizard, "4, 4");

        JPanel slotsPanel = new JPanel();
        slotsPanel.setBorder(new TitledBorder(null, "Slot Locations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(slotsPanel);
        slotsPanel.setLayout(new FormLayout(new ColumnSpec[]{
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("4dlu:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,},
                new RowSpec[]{
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        RowSpec.decode("4dlu:grow"),
                        FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC,}));

        PhotonFeederSlots feederSlots = photonProperties.getFeederSlots();
        slotsTableModel = new PhotonFeederSlotsTableModel(feederSlots);
        slotsTable = new AutoSelectTextTable(slotsTableModel);
        slotsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        slotsTable.setDefaultRenderer(LengthCellValue.class, new MonospacedFontTableCellRenderer());
        slotsTable.setDefaultRenderer(RotationCellValue.class, new MonospacedFontTableCellRenderer());

        TableRowSorter<PhotonFeederSlotsTableModel> sorter = new TableRowSorter<>(slotsTableModel);
        slotsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(slotsTable);
        slotsPanel.add(scrollPane, "2, 2, 1, 3, fill, fill");

        moveCameraButton = new JButton(Icons.centerCamera);
        moveCameraButton.setToolTipText("Move Camera to Slot");
        moveCameraButton.addActionListener(moveCameraAction);
        slotsPanel.add(moveCameraButton, "4, 6");

        captureSlotLocationButton = new JButton(Icons.captureCamera);
        captureSlotLocationButton.setToolTipText("Capture Camera Location");
        captureSlotLocationButton.addActionListener(captureSlotLocationAction);
        slotsPanel.add(captureSlotLocationButton, "6, 6");
    }

    @Override
    public void createBindings() {
        bind(UpdateStrategy.READ_WRITE, photonProperties, "maxFeederAddress", maxFeederSpinner, "value");
    }

    private final Action searchAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            progressBarPanel.setVisible(true);
            searchButton.setEnabled(false);
            maxFeederSpinner.setEnabled(false);

            int maxFeederAddress = photonProperties.getMaxFeederAddress();
            progressBarPanel.setNumberOfElements(maxFeederAddress);

            UiUtils.submitUiMachineTask(() -> {
                PhotonFeeder.findAllFeeders(progressBarPanel::updateFeederState);
                return null;
            }, (parameter) -> {
                resetState();
            }, (throwable) -> {
                resetState();

                MessageBoxes.errorBox(MainFrame.get(), "Error", throwable);
            });
        }

        private void resetState() {
            progressBarPanel.setVisible(false);
            progressBarPanel.clearAllState();
            searchButton.setEnabled(true);
            maxFeederSpinner.setEnabled(true);
            slotsTableModel.refresh();
        }
    };

    private final Action moveCameraAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = slotsTable.getSelectedRow();
            if (selectedRow < 0) {
                MessageBoxes.errorBox(MainFrame.get(), "Error", new Exception("Please select a slot."));
                return;
            }

            UiUtils.messageBoxOnException(() -> {
                int modelRow = slotsTable.convertRowIndexToModel(selectedRow);
                Object obj = slotsTableModel.getRowObjectAt(modelRow);
                if (obj instanceof PhotonFeederSlots.Slot) {
                    PhotonFeederSlots.Slot slot = (PhotonFeederSlots.Slot) obj;
                    org.openpnp.model.Location location = slot.getLocation();
                    if (location == null) {
                        MessageBoxes.errorBox(MainFrame.get(), "Error", new Exception("Slot location not set."));
                        return;
                    }

                    UiUtils.submitUiMachineTask(() -> {
                        org.openpnp.spi.Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
                        MovableUtils.moveToLocationAtSafeZ(camera, location);
                        return null;
                    });
                }
            });
        }
    };

    private final Action captureSlotLocationAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = slotsTable.getSelectedRow();
            if (selectedRow < 0) {
                MessageBoxes.errorBox(MainFrame.get(), "Error", new Exception("Please select a slot to capture location."));
                return;
            }

            UiUtils.messageBoxOnException(() -> {
                int modelRow = slotsTable.convertRowIndexToModel(selectedRow);
                Object obj = slotsTableModel.getRowObjectAt(modelRow);
                if (obj instanceof PhotonFeederSlots.Slot) {
                    PhotonFeederSlots.Slot slot = (PhotonFeederSlots.Slot) obj;
                    UiUtils.submitUiMachineTask(() -> {
                        org.openpnp.spi.Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
                        org.openpnp.model.Location location = camera.getLocation();
                        slot.setLocation(location);
                        SwingUtilities.invokeLater(() -> slotsTableModel.refresh());
                    });
                }
            });
        }
    };
}
