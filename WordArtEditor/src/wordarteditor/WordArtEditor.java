import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WordArtEditor extends JFrame {

    private WordArtCanvas canvas;
    private JTextField txtInput;
    private JComboBox<String> cmbFonts;
    private JComboBox<String> cmbStyle;
    private JSlider sldSize, sldShadowX, sldShadowY, sldPerspective;
    private JButton btnTextColor, btnShadowColor;
    
    Color textColor = Color.BLUE;
    Color shadowColor = Color.GRAY;

    public WordArtEditor() {
        setTitle("Editor WordArt 2D con Perspectiva y Sombra");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel de Dibujo (Lienzo)
        canvas = new WordArtCanvas();
        add(canvas, BorderLayout.CENTER);

        // Panel de Controles
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(4, 2, 5, 5));

        // 1. Texto de entrada
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("Texto:"));
        txtInput = new JTextField("WordArt 2D", 10);
        txtInput.addActionListener(e -> canvas.repaint());
        p1.add(txtInput);
        controlPanel.add(p1);

        // 2. Fuente y Estilo
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        cmbFonts = new JComboBox<>(fonts);
        cmbFonts.setSelectedItem("Arial");
        cmbFonts.addActionListener(e -> canvas.repaint());
        p2.add(cmbFonts);

        cmbStyle = new JComboBox<>(new String[]{"Normal", "Negrita", "Cursiva"});
        cmbStyle.addActionListener(e -> canvas.repaint());
        p2.add(cmbStyle);
        controlPanel.add(p2);

        // 3. Tamaño y Colores
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p3.add(new JLabel("Tamaño:"));
        sldSize = new JSlider(20, 100, 50);
        sldSize.setPreferredSize(new Dimension(80, 20));
        sldSize.addChangeListener(e -> canvas.repaint());
        p3.add(sldSize);

        btnTextColor = new JButton("Color");
        btnTextColor.setBackground(textColor);
        btnTextColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Elija Color de Texto", textColor);
            if (c != null) {
                textColor = c;
                btnTextColor.setBackground(c);
                canvas.repaint();
            }
        });
        p3.add(btnTextColor);
        controlPanel.add(p3);

        // 4. Efectos (Perspectiva y Sombra)
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p4.add(new JLabel("Perspectiva:"));
        sldPerspective = new JSlider(-50, 50, 0);
        sldPerspective.setPreferredSize(new Dimension(80, 20));
        sldPerspective.addChangeListener(e -> canvas.repaint());
        p4.add(sldPerspective);

        p4.add(new JLabel("Sombra X/Y:"));
        sldShadowX = new JSlider(-20, 20, 5);
        sldShadowX.setPreferredSize(new Dimension(50, 20));
        sldShadowX.addChangeListener(e -> canvas.repaint());
        p4.add(sldShadowX);

        controlPanel.add(p4);

        add(controlPanel, BorderLayout.SOUTH);
    }

    // Clase interna para el lienzo gráfico 2D
    class WordArtCanvas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Mejorar calidad de renderizado (Antialiasing)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fondo blanco
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            String text = txtInput.getText();
            if (text.isEmpty()) return;

            // Configurar Fuente
            String fontName = (String) cmbFonts.getSelectedItem();
            int styleInt = Font.PLAIN;
            String styleStr = (String) cmbStyle.getSelectedItem();
            if ("Negrita".equals(styleStr)) styleInt = Font.BOLD;
            else if ("Cursiva".equals(styleStr)) styleInt = Font.ITALIC;

            int size = sldSize.getValue();
            Font font = new Font(fontName, styleInt, size);
            g2d.setFont(font);

            // Posición inicial en el centro
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout tl = new TextLayout(text, font, frc);
            Rectangle2D bounds = tl.getBounds();

            float x = (float) (getWidth() - bounds.getWidth()) / 2;
            float y = (float) (getHeight() + bounds.getHeight()) / 2;

            // --- APLICAR EFECTO DE SOMBRA ---
            int shadowX = sldShadowX.getValue();
            int shadowY = sldShadowX.getValue(); // Usamos valor simple para Y o creamos otro slider
            g2d.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 150));
            
            AffineTransform shadowTransform = new AffineTransform();
            shadowTransform.translate(x + shadowX, y + shadowY);
            
            // Aplicar perspectiva también a la sombra si es necesario
            double perspective = sldPerspective.getValue() / 100.0;
            shadowTransform.shear(0, perspective);

            Shape shadowedShape = tl.getOutline(shadowTransform);
            g2d.fill(shadowedShape);

            // --- APLICAR TEXTO PRINCIPAL CON PERSPECTIVA ---
            AffineTransform textTransform = new AffineTransform();
            textTransform.translate(x, y);
            textTransform.shear(0, perspective); // Efecto de perspectiva (Deformación trapezoidal)

            Shape textShape = tl.getOutline(textTransform);

            // Relleno degradado o sólido estilo WordArt
            g2d.setColor(textColor);
            g2d.fill(textShape);

            // Borde del texto para realzar el estilo WordArt
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(textShape);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WordArtEditor().setVisible(true);
        });
    }
}
