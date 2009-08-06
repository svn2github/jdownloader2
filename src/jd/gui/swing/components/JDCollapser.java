//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.swing.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.UIManager;

import jd.gui.swing.jdgui.InfoPanelHandler;
import jd.gui.swing.jdgui.interfaces.DroppedPanel;
import jd.gui.swing.jdgui.interfaces.SwitchPanel;
import jd.utils.locale.JDL;
import net.miginfocom.swing.MigLayout;

/**
 * class for an infopanel with close button.
 * 
 * @author Coalado
 * 
 */
public class JDCollapser extends DroppedPanel {

    private static final long serialVersionUID = 6864885344815243560L;
    private static JDCollapser INSTANCE = null;

    public static JDCollapser getInstance() {
        if (INSTANCE == null) INSTANCE = new JDCollapser();
        return INSTANCE;
    }

    private SwitchPanel panel;

    private JPanel content;
    private JMenuBar menubar;
    private JButton closeButton;
    private JButton menutitle;

    private JDCollapser() {
        super();
        this.setLayout(new MigLayout("ins 0 5 0 0,wrap 1", "[fill,grow]", "[fill,grow]"));

        menubar = new JMenuBar();
        menubar.add(menutitle = new JButton(""));
        menutitle.setContentAreaFilled(false);
        menutitle.setBorderPainted(false);
        menubar.add(Box.createHorizontalGlue());

        CloseAction closeAction = new CloseAction();
        Box panel = new Box(1);
        // JButton bt;

        panel.add(closeButton = new JButton(closeAction));

        closeButton.setPreferredSize(new Dimension(20, 14));
        // closeButton.setContentAreaFilled(false);
        closeButton.setToolTipText(JDL.LF("jd.gui.swing.components.JDCollapser.closetooltip", "Close %s", ""));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 5));
        menubar.add(panel);

        add(menubar, "dock NORTH,height 18!,gapbottom 2");

        content = new JPanel();
        add(content);
        this.setVisible(true);

    }

    public void paint(Graphics g) {
        super.paint(g);

    }

    public void setContentPanel(SwitchPanel panel2, String name, ImageIcon icon) {
        if (panel2 == this.panel) return;
        menutitle.setText(name);
        menutitle.setIcon(icon);
        this.closeButton.setToolTipText(JDL.LF("jd.gui.swing.components.JDCollapser.closetooltip", "Close %s", name));
        content.removeAll();
        this.panel = panel2;
        if (panel == null) return;
        content.setLayout(new MigLayout("ins 0,wrap 1", "[fill,grow]", "[fill,grow]"));

        content.add(panel);
        revalidate();
        content.revalidate();
    }

    public SwitchPanel getContentPanel() {
        return panel;
    }

    @Override
    /**
     * deligates the onHidevenet to the contentpanel
     */
    public void onHide() {
        if (panel != null) panel.setHidden();
    }

    @Override
    /**
     * deligates the onShow event to the contentpanel
     */
    public void onShow() {
        if (panel != null) panel.setShown();

    }

    public class CloseAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = -771203720364300914L;

        public CloseAction() {

            // this.putValue(AbstractAction.SMALL_ICON,
            // UIManager.getIcon("Synthetica.docking.titlebar.close.hover"));

            this.putValue(AbstractAction.SMALL_ICON, UIManager.getIcon("InternalFrame.closeIcon"));
        }

        public void actionPerformed(ActionEvent e) {
            InfoPanelHandler.setPanel(null);
        }
    }

}
