package mn.num.hospital.icd10_service.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import mn.num.hospital.icd10_service.dto.Diagnosis;
import mn.num.hospital.icd10_service.dto.DiagnosisResponse;
import mn.num.hospital.icd10_service.dto.ICD10CodeDTO;
import mn.num.hospital.icd10_service.service.DiagnosisService;
public class DiagnosisSearchUI {

    private JFrame frame;
    private JTextField searchField;
    private JTextField limitField;
    private DefaultListModel<Diagnosis> listModel;
    private JList<Diagnosis> resultList;
    private JLabel countLabel;

    private final String API_URL = "http://localhost:8080/api/diagnosis/suggest";
	private DiagnosisService diagnosisService;

    public DiagnosisSearchUI(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
        frame = new JFrame("Онош хайх");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(1,2,10,10));
        main.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // LEFT
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        searchField = new JTextField();
        limitField = new JTextField("5");

        JButton searchBtn = new JButton("Хайх");

        left.add(new JLabel("Түлхүүр үг"));
        left.add(searchField);
        left.add(new JLabel("Limit"));
        left.add(limitField);
        left.add(searchBtn);

        // RIGHT
        JPanel right = new JPanel(new BorderLayout());
        countLabel = new JLabel("Хайлтын илэрц: 0");

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);

        // Renderer (detail харуулна)
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean hasFocus) {

                Diagnosis d = (Diagnosis) value;

                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

                JLabel title = new JLabel(d.code + " - " + d.name);
                title.setFont(new Font("Arial", Font.BOLD, 14));

                p.add(title);

                if (d.detail != null) {
                    JLabel det = new JLabel(d.detail);
                    det.setFont(new Font("Arial", Font.PLAIN, 12));
                    det.setForeground(Color.GRAY);
                    p.add(det);
                }

                if (isSelected) p.setBackground(new Color(200,230,255));

                return p;
            }
        });

        right.add(countLabel, BorderLayout.NORTH);
        right.add(new JScrollPane(resultList), BorderLayout.CENTER);

        main.add(left);
        main.add(right);

        frame.add(main);

        // EVENTS
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Diagnosis d = resultList.getSelectedValue();
                if (d != null) {
                    JOptionPane.showMessageDialog(frame,
                        "Сонгосон: " + d.code + " - " + d.name);
                }
            }
        });

        frame.setVisible(true);
    }

    private List<Diagnosis> fetchFromService(String query, int limit) {

        List<Diagnosis> result = new ArrayList<>();

        try {
            DiagnosisResponse response =
                    diagnosisService.suggestCodes(query, limit);

            if (response.isSuccess()) {

                for (ICD10CodeDTO dto : response.getSuggestions()) {

                    Diagnosis d = new Diagnosis(dto.getCode(),dto.getName(), dto.getDetail());

                    result.add(d);
                }
            } else {
                JOptionPane.showMessageDialog(frame, response.getMessage());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Алдаа: " + e.getMessage());
        }

        return result;
    }


    private void search() {
        String query = searchField.getText().trim();
        int limit = Integer.parseInt(limitField.getText());

        listModel.clear();

        if (query.isEmpty()) return;

        new Thread(() -> {
            List<Diagnosis> data = fetchFromService(query, limit);

            SwingUtilities.invokeLater(() -> {
                for (Diagnosis d : data) {
                    listModel.addElement(d);
                }
                countLabel.setText("Хайлтын илэрц: " + data.size());
            });
        }).start();
    }
}