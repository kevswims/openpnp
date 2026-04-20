package org.openpnp.machine.photon.sheets.gui;

import java.util.ArrayList;
import java.util.List;

import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.gui.support.RotationCellValue;
import org.openpnp.gui.tablemodel.AbstractObjectTableModel;
import org.openpnp.machine.photon.PhotonFeederSlots;
import org.openpnp.machine.photon.PhotonFeederSlots.Slot;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Location;
import org.openpnp.util.Utils2D;

public class PhotonFeederSlotsTableModel extends AbstractObjectTableModel {
    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {"Slot", "X", "Y", "Z", "Rotation"};
    private static final Class<?>[] COLUMN_TYPES = {
            Integer.class, LengthCellValue.class, LengthCellValue.class, LengthCellValue.class, RotationCellValue.class
    };

    private final PhotonFeederSlots feederSlots;
    private List<PhotonFeederSlots.Slot> slots = new ArrayList<>();

    public PhotonFeederSlotsTableModel(PhotonFeederSlots feederSlots) {
        this.feederSlots = feederSlots;
        refresh();
    }

    public void refresh() {
        slots = new ArrayList<>(feederSlots.getSlots());
        slots.sort((a, b) -> Integer.compare(a.getAddress(), b.getAddress()));
        fireTableDataChanged();
    }

    @Override
    public Object getRowObjectAt(int index) {
        if (index >= 0 && index < slots.size()) {
            return slots.get(index);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return slots.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return COLUMN_TYPES[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col > 0; // Only address (col 0) is read-only
    }

    @Override
    public Object getValueAt(int row, int col) {
        Slot slot = slots.get(row);
        Location loc = slot.getLocation();

        switch (col) {
            case 0:
                return slot.getAddress();
            case 1:
                if (loc == null) {
                    return new LengthCellValue(new Length(0, Configuration.get().getSystemUnits()), true, true);
                }
                return new LengthCellValue(loc.getLengthX(), true, true);
            case 2:
                if (loc == null) {
                    return new LengthCellValue(new Length(0, Configuration.get().getSystemUnits()), true, true);
                }
                return new LengthCellValue(loc.getLengthY(), true, true);
            case 3:
                if (loc == null) {
                    return new LengthCellValue(new Length(0, Configuration.get().getSystemUnits()), true, true);
                }
                return new LengthCellValue(loc.getLengthZ(), true, true);
            case 4:
                if (loc == null) {
                    return new RotationCellValue(0.0);
                }
                return new RotationCellValue(loc.getRotation(), false, true);
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int colIndex) {
        Slot slot = slots.get(rowIndex);
        Location loc = slot.getLocation();
        if (loc == null) {
            loc = new Location(Configuration.get().getSystemUnits());
        }

        try {
            if (colIndex == 1 || colIndex == 2 || colIndex == 3) {
                LengthCellValue lcv = (LengthCellValue) aValue;
                Length length = lcv.getLength();
                Length.Field field = Length.Field.values()[colIndex - 1]; // X=0, Y=1, Z=2
                loc = Length.setLocationField(Configuration.get(), loc, length, field);
            } else if (colIndex == 4) {
                RotationCellValue rcv = (RotationCellValue) aValue;
                loc = loc.derive(null, null, null, rcv.getRotation());
            }

            slot.setLocation(loc);
            fireTableRowsUpdated(rowIndex, rowIndex);
        } catch (Exception e) {
            // Invalid input; ignore update
        }
    }
}
