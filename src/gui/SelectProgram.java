//-----------------------------------------------------------------------------
// $Id$
// $Source$
//-----------------------------------------------------------------------------

package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;
import utils.GuiUtils;

//-----------------------------------------------------------------------------

class SelectProgram
    extends JDialog
    implements ActionListener, KeyListener
{
    public SelectProgram(Frame owner)
    {
        super(owner, "GoGui: Select program", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.add(createCommandPanel(), BorderLayout.CENTER);
        contentPane.add(createButtons(), BorderLayout.SOUTH);
        pack();
    }

    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();
        if (command.equals("cancel"))
            dispose();
        else if (command.equals("comboBoxEdited")
                 || command.equals("ok"))
        {
            m_command = m_comboBox.getSelectedItem().toString();
            m_comboBox.insertItemAt(m_command, 0);
            saveHistory(getHistory());
            dispose();
        }
        else if (command.equals("open"))
            open();
    }

    public static void addHistory(String program)
    {
        String p = program.trim();
        if (p.equals(""))
            return;
        Vector history = loadHistory();
        history.add(0, p);
        saveHistory(history);
    }

    public void keyPressed(KeyEvent e)
    {
        int c = e.getKeyCode();        
        if (c == KeyEvent.VK_ESCAPE)
        {
            if (! m_comboBox.isPopupVisible())
                dispose();
        }
    }

    public void keyReleased(KeyEvent e) 
    {
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public static String select(Frame owner)
    {
        SelectProgram dialog = new SelectProgram(owner);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return dialog.m_command;
    }

    private JComboBox m_comboBox;

    private JTextField m_textField;

    private String m_command;

    private JPanel createButtons()
    {
        JPanel innerPanel = new JPanel(new GridLayout(1, 0, GuiUtils.PAD, 0));
        innerPanel.setBorder(GuiUtils.createEmptyBorder());
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        innerPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        innerPanel.add(cancelButton);
        JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outerPanel.add(innerPanel);
        return outerPanel;
    }

    private JPanel createCommandPanel()
    {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(GuiUtils.createEmptyBorder());
        JLabel label = new JLabel("Go program command");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(label);
        panel.add(createInputPanel());
        return panel;
    }

    private JPanel createInputPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());
        m_comboBox = new JComboBox(loadHistory());
        m_comboBox.addActionListener(this);
        m_comboBox.setEditable(true);
        ComboBoxEditor editor = m_comboBox.getEditor();
        m_textField = (JTextField)editor.getEditorComponent();
        m_textField.setColumns(40);
        m_textField.selectAll();
        m_textField.addKeyListener(this);
        int fontSize = m_comboBox.getFont().getSize();
        m_comboBox.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        innerPanel.add(m_comboBox, BorderLayout.CENTER);
        URL u = getClass().getClassLoader().getResource("images/fileopen.png");
        JButton button = new JButton(new ImageIcon(u));
        button.setActionCommand("open");
        button.addActionListener(this);
        innerPanel.add(button, BorderLayout.EAST);
        outerPanel.add(innerPanel, BorderLayout.NORTH);
        return outerPanel;
    }

    private Vector getHistory()
    {
        Vector result = new Vector(32, 32);
        int maxHistory = 10;
        int itemCount = m_comboBox.getItemCount();
        int n = itemCount;
        if (n > maxHistory)
            n = maxHistory;
        for (int i = 0; i < n; ++i)
            result.add(m_comboBox.getItemAt(i).toString().trim());
        return result;
    }

    private static File getHistoryFile()
    {
        String home = System.getProperty("user.home");
        File dir = new File(home, ".gogui");
        if (! dir.exists())
            dir.mkdir();
        return new File(dir, "program-history");
    }

    private static Vector loadHistory()
    {
        Vector result = new Vector(32, 32);
        File file = getHistoryFile();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            while (line != null)
            {
                line = line.trim();
                if (! result.contains(line))
                    result.add(line);
                line = in.readLine();
            }
        }
        catch (IOException e)
        {
        }
        if (! result.contains("gnugo --mode gtp -r %SRAND"))
            result.add("gnugo --mode gtp -r %SRAND");
        return result;
    }

    private void open()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File file = chooser.getSelectedFile();
        m_comboBox.insertItemAt(file.toString(), 0);
        m_comboBox.setSelectedIndex(0);
        m_textField.setCaretPosition(m_textField.getText().length());
        m_textField.requestFocus();
    }

    private static void saveHistory(Vector history)
    {
        File file = getHistoryFile();
        try
        {
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            int size = history.size();
            for (int i = 0; i < size; ++i)
                out.println((String)history.get(i));
            out.close();
        }
        catch (FileNotFoundException e)
        {
        }

    }
}

//-----------------------------------------------------------------------------
