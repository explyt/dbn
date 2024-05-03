package com.dbn.oracleAI.ui;

import com.dbn.common.icon.Icons;
import com.dbn.oracleAI.types.AuthorType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * A custom auto-wrapped text pane component with an integrated copy button.
 * It visually differentiates messages from the user and AI and provides functionality to copy text to the clipboard.
 */
public class JIMSendTextPane extends JPanel {

  private JTextPane textPane;

  /**
   * Constructor that sets up the text pane and copy button.
   */
  public JIMSendTextPane() {
    setLayout(new BorderLayout());
    initializeTextPane();
    initializeCopyButton();
    setOpaque(false);
  }

  /**
   * Initializes the JTextPane with appropriate styling and editor kit.
   */
  private void initializeTextPane() {
    textPane = new JTextPane();
    textPane.setEditorKit(new WarpEditorKit());
    textPane.setOpaque(false);
    textPane.setEditable(false);
    textPane.setForeground(Color.BLACK);
    textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(textPane, BorderLayout.CENTER);
  }

  /**
   * Creates and configures the copy button and its panel.
   * TODO handling colors based on theme
   */
  private void initializeCopyButton() {
    JButton button = new JButton(Icons.ACTION_COPY);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(30, 30));
    button.addActionListener(e -> copyTextToClipboard());

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setOpaque(false);
    buttonPanel.add(button, BorderLayout.NORTH);
    buttonPanel.setPreferredSize(new Dimension(30, 30));

    add(buttonPanel, BorderLayout.EAST);
  }

  /**
   * Copies the current text in the JTextPane to the system clipboard.
   */
  private void copyTextToClipboard() {
    String text = textPane.getText();
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  /**
   * Sets the author of the message, changing the background color according to the author type.
   *
   * @param author the type of the author (USER or AI)
   */
  public void setAuthor(AuthorType author) {
    setBackground(author == AuthorType.USER ? new Color(179, 233, 255) : new Color(242, 242, 242));
  }

  /**
   * Sets the text of the JTextPane.
   *
   * @param text the text to be displayed
   */
  public void setText(String text) {
    textPane.setText(text);
  }

  /**
   * Retrieves the text from the JTextPane.
   *
   * @return the current text
   */
  public String getText() {
    return textPane.getText();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
    g2.dispose();
  }

  private class WarpEditorKit extends StyledEditorKit {
    private final ViewFactory defaultFactory = new WarpColumnFactory();

    @Override
    public ViewFactory getViewFactory() {
      return defaultFactory;
    }
  }

  private class WarpColumnFactory implements ViewFactory {
    public View create(Element elem) {
      String kind = elem.getName();
      if (kind != null) {
        switch (kind) {
          case AbstractDocument.ContentElementName:
            return new WarpLabelView(elem);
          case AbstractDocument.ParagraphElementName:
            return new ParagraphView(elem);
          case AbstractDocument.SectionElementName:
            return new BoxView(elem, View.Y_AXIS);
          case StyleConstants.ComponentElementName:
            return new ComponentView(elem);
          case StyleConstants.IconElementName:
            return new IconView(elem);
          default:
            return new LabelView(elem);
        }
      }
      return new LabelView(elem);
    }
  }

  private class WarpLabelView extends LabelView {
    public WarpLabelView(Element elem) {
      super(elem);
    }

    @Override
    public float getMinimumSpan(int axis) {
      if (axis == View.X_AXIS) {
        return 0;
      } else if (axis == View.Y_AXIS) {
        return super.getMinimumSpan(axis);
      } else {
        throw new IllegalArgumentException("Invalid axis: " + axis);
      }
    }
  }
}
