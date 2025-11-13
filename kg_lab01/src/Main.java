import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Main extends JFrame {
    private JTextField rField, gField, bField;
    private JTextField cField, mField, yField, kField;
    private JTextField hField, sField, vField;
    private JSlider rSlider, gSlider, bSlider;
    private JSlider cSlider, mSlider, ySlider, kSlider;
    private JSlider hSlider, sSlider, vSlider;
    private JPanel colorPreview;
    private boolean isUpdating = false;

    public Main() {
        setTitle("RGB-CMYK-HSV");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        main.add(createColorPickerPanel());
        main.add(Box.createRigidArea(new Dimension(0, 10)));

        main.add(createPanel(
                "RGB модель",
                new String[]{"R (0-255)", "G (0-255)", "B (0-255)"},
                new JTextField[]{rField = field(), gField = field(), bField = field()},
                new JSlider[]{rSlider = slider(255), gSlider = slider(255), bSlider = slider(255)},
                this::updateFromRGB
        ));
        main.add(Box.createRigidArea(new Dimension(0, 10)));

        main.add(createPanel(
                "CMYK модель",
                new String[]{"C (0-100)", "M (0-100)", "Y (0-100)", "K (0-100)"},
                new JTextField[]{cField = field(), mField = field(), yField = field(), kField = field()},
                new JSlider[]{cSlider = slider(100), mSlider = slider(100), ySlider = slider(100), kSlider = slider(100)},
                this::updateFromCMYK
        ));
        main.add(Box.createRigidArea(new Dimension(0, 10)));

        main.add(createPanel(
                "HSV модель",
                new String[]{"H (0-360)", "S (0-100)", "V (0-100)"},
                new JTextField[]{hField = field(), sField = field(), vField = field()},
                new JSlider[]{hSlider = slider(360), sSlider = slider(100), vSlider = slider(100)},
                this::updateFromHSV
        ));

        add(main);
        pack();
        setLocationRelativeTo(null);

        setValues(
                new JTextField[]{cField, mField, yField, kField},
                new JSlider[]{cSlider, mSlider, ySlider, kSlider},
                new int[]{0, 0, 0, 100}
        );
    }

    private JPanel createColorPickerPanel() {
        JButton picker = new JButton("Выбрать цвет из палитры");
        picker.addActionListener(e -> {
            Color currentColor = new Color(val(rField), val(gField), val(bField));
            Color selectedColor = JColorChooser.showDialog(this, "Выберите цвет", currentColor);

            if (selectedColor != null) {
                setValues(
                        new JTextField[]{rField, gField, bField},
                        new JSlider[]{rSlider, gSlider, bSlider},
                        new int[]{selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()}
                );
            }
        });

        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(600, 60));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Выбор цвета"));
        panel.add(picker, BorderLayout.NORTH);
        panel.add(colorPreview, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPanel(String title, String[] labels, JTextField[] fields, JSlider[] sliders, Runnable update) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(fields[i]);
            panel.add(sliders[i]);

            int idx = i;

            sliders[i].addChangeListener(e -> {
                if (!isUpdating) {
                    fields[idx].setText(String.valueOf(sliders[idx].getValue()));
                    update.run();
                }
            });

            fields[i].getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    sync();
                }

                public void removeUpdate(DocumentEvent e) {
                    sync();
                }

                public void insertUpdate(DocumentEvent e) {
                    sync();
                }

                private void sync() {
                    if (!isUpdating) {
                        try {
                            int value = Integer.parseInt(fields[idx].getText());
                            int min = sliders[idx].getMinimum();
                            int max = sliders[idx].getMaximum();
                            int clampedValue = Math.max(min, Math.min(value, max));
                            sliders[idx].setValue(clampedValue);
                            update.run();
                        } catch (NumberFormatException ex) {
                        }
                    }
                }
            });
        }

        return panel;
    }

    private JTextField field() {
        JTextField f = new JTextField("0", 10);
        f.setHorizontalAlignment(JTextField.CENTER);
        return f;
    }

    private JSlider slider(int max) {
        return new JSlider(0, max, 0);
    }

    private int val(JTextField f) {
        try {
            return Integer.parseInt(f.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setValues(JTextField[] fields, JSlider[] sliders, int[] values) {
        isUpdating = true;
        for (int i = 0; i < values.length; i++) {
            fields[i].setText(String.valueOf(values[i]));
            sliders[i].setValue(values[i]);
        }
        isUpdating = false;

        if (fields[0] == rField) {
            updateFromRGB();
        } else if (fields[0] == cField) {
            updateFromCMYK();
        } else {
            updateFromHSV();
        }
    }

    private void updateFields(JTextField[] fields, JSlider[] sliders, int[] values) {
        for (int i = 0; i < values.length; i++) {
            fields[i].setText(String.valueOf(values[i]));
            sliders[i].setValue(values[i]);
        }
    }

    private void updateFromRGB() {
        if (isUpdating) return;
        isUpdating = true;

        int r = val(rField);
        int g = val(gField);
        int b = val(bField);

        int[] cmyk = rgbToCmyk(r, g, b);
        int[] hsv = rgbToHsv(r, g, b);

        updateFields(
                new JTextField[]{cField, mField, yField, kField},
                new JSlider[]{cSlider, mSlider, ySlider, kSlider},
                cmyk
        );
        updateFields(
                new JTextField[]{hField, sField, vField},
                new JSlider[]{hSlider, sSlider, vSlider},
                hsv
        );

        colorPreview.setBackground(new Color(r, g, b));
        isUpdating = false;
    }

    private void updateFromCMYK() {
        if (isUpdating) return;
        isUpdating = true;

        int c = val(cField);
        int m = val(mField);
        int y = val(yField);
        int k = val(kField);

        int[] rgb = cmykToRgb(c, m, y, k);
        int[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);

        updateFields(
                new JTextField[]{rField, gField, bField},
                new JSlider[]{rSlider, gSlider, bSlider},
                rgb
        );
        updateFields(
                new JTextField[]{hField, sField, vField},
                new JSlider[]{hSlider, sSlider, vSlider},
                hsv
        );

        colorPreview.setBackground(new Color(rgb[0], rgb[1], rgb[2]));
        isUpdating = false;
    }

    private void updateFromHSV() {
        if (isUpdating) return;
        isUpdating = true;

        int h = val(hField);
        int s = val(sField);
        int v = val(vField);

        int[] rgb = hsvToRgb(h, s, v);
        int[] cmyk = rgbToCmyk(rgb[0], rgb[1], rgb[2]);

        updateFields(
                new JTextField[]{rField, gField, bField},
                new JSlider[]{rSlider, gSlider, bSlider},
                rgb
        );
        updateFields(
                new JTextField[]{cField, mField, yField, kField},
                new JSlider[]{cSlider, mSlider, ySlider, kSlider},
                cmyk
        );

        colorPreview.setBackground(new Color(rgb[0], rgb[1], rgb[2]));
        isUpdating = false;
    }

    private int[] rgbToCmyk(int r, int g, int b) {
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;

        double k = 1 - Math.max(Math.max(rNorm, gNorm), bNorm);

        if (k == 1) {
            return new int[]{0, 0, 0, 100};
        }

        double c = (1 - rNorm - k) / (1 - k);
        double m = (1 - gNorm - k) / (1 - k);
        double y = (1 - bNorm - k) / (1 - k);

        return new int[]{
                (int) Math.round(c * 100),
                (int) Math.round(m * 100),
                (int) Math.round(y * 100),
                (int) Math.round(k * 100)
        };
    }

    private int[] cmykToRgb(int c, int m, int y, int k) {
        double cNorm = c / 100.0;
        double mNorm = m / 100.0;
        double yNorm = y / 100.0;
        double kNorm = k / 100.0;

        int r = (int) Math.round(255 * (1 - cNorm) * (1 - kNorm));
        int g = (int) Math.round(255 * (1 - mNorm) * (1 - kNorm));
        int b = (int) Math.round(255 * (1 - yNorm) * (1 - kNorm));

        return new int[]{r, g, b};
    }

    private int[] rgbToHsv(int r, int g, int b) {
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;

        double max = Math.max(Math.max(rNorm, gNorm), bNorm);
        double min = Math.min(Math.min(rNorm, gNorm), bNorm);
        double diff = max - min;

        double h = 0;
        double s = 0;
        double v = max;

        if (diff != 0) {
            s = diff / max;

            if (rNorm == max) {
                h = (gNorm - bNorm) / diff;
            } else if (gNorm == max) {
                h = 2 + (bNorm - rNorm) / diff;
            } else {
                h = 4 + (rNorm - gNorm) / diff;
            }

            h = h / 6.0;

            if (h < 0) {
                h += 1;
            } else if (h > 1) {
                h -= 1;
            }
        }

        return new int[]{
                (int) Math.round(h * 360),
                (int) Math.round(s * 100),
                (int) Math.round(v * 100)
        };
    }

    private int[] hsvToRgb(int h, int s, int v) {
        double sNorm = s / 100.0;
        double vNorm = v / 100.0;

        double c = vNorm * sNorm;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = vNorm - c;

        double rPrime = 0;
        double gPrime = 0;
        double bPrime = 0;

        if (h >= 0 && h < 60) {
            rPrime = c;
            gPrime = x;
            bPrime = 0;
        } else if (h >= 60 && h < 120) {
            rPrime = x;
            gPrime = c;
            bPrime = 0;
        } else if (h >= 120 && h < 180) {
            rPrime = 0;
            gPrime = c;
            bPrime = x;
        } else if (h >= 180 && h < 240) {
            rPrime = 0;
            gPrime = x;
            bPrime = c;
        } else if (h >= 240 && h < 300) {
            rPrime = x;
            gPrime = 0;
            bPrime = c;
        } else {
            rPrime = c;
            gPrime = 0;
            bPrime = x;
        }

        int r = (int) Math.round((rPrime + m) * 255);
        int g = (int) Math.round((gPrime + m) * 255);
        int b = (int) Math.round((bPrime + m) * 255);

        return new int[]{r, g, b};
    }

    public static void main(String[] args) {
            Main frame = new Main();
            frame.setVisible(true);
    }
}
