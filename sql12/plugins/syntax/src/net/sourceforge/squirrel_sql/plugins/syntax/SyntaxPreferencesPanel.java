package net.sourceforge.squirrel_sql.plugins.syntax;
/*
 * Copyright (C) 2003 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.awt.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;
import net.sourceforge.squirrel_sql.fw.gui.MultipleLineLabel;

import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.preferences.INewSessionPropertiesPanel;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.properties.ISessionPropertiesPanel;

import net.sourceforge.squirrel_sql.plugins.syntax.prefspanel.StyleMaintenancePanel;
import net.sourceforge.squirrel_sql.plugins.syntax.prefspanel.StylesList;
/**
 * New Session and Current Session preferences panel for this plugin.
 *
 * @author <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class SyntaxPreferencesPanel
	implements INewSessionPropertiesPanel, ISessionPropertiesPanel
{
	private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(SyntaxPreferencesPanel.class);


	/** Logger for this class. */
	private static final ILogger s_log =
		LoggerController.createLogger(SyntaxPreferencesPanel.class);

	/** Plugin preferences object. */
	private final SyntaxPreferences _prefs;

	/** Component to display in the preferences dialog. */
	private final MyPanel _myPanel;

	/**
	 * Ctor.
	 *
	 * @param	prefs	The preferences to be maintained.
	 *
	 * @throws	IllegalArgumentException
	 *			if <TT>prefs</TT> is <TT>null</TT>.
	 */
	public SyntaxPreferencesPanel(SyntaxPreferences prefs, SyntaxPluginResources rsrc)
	{
		if (prefs == null)
		{
			throw new IllegalArgumentException("Null SyntaxPreferences passed");
		}
		_prefs = prefs;

		// Create the actual panel that will be displayed in dialog.
		_myPanel = new MyPanel(prefs, rsrc);
	}

	/**
	 * Panel is being loaded for the Application Preferences. This means that
	 * the settings are for newly created sessions.
	 *
	 * @param	app	 Application API.
	 */
	public void initialize(IApplication app)
	{
		_myPanel.loadData(_prefs);
	}

	/**
	 * Panel is being loaded for the Session Properties. This means that
	 * the settings are for the current session only.
	 *
	 * @param	app	 Application API.
	 *
	 * @throws	IllegalArgumentException
	 *			if <TT>IApplication</TT> is <TT>null</TT>.
	 */
	public void initialize(IApplication app, ISession session)
	{
		if (app == null)
		{
			throw new IllegalArgumentException("Null IApplication passed");
		}
		if (session == null)
		{
			throw new IllegalArgumentException("Null ISession passed");
		}

		_myPanel.loadData(_prefs);
	}

	/**
	 * Return the component to be displayed in the Preferences dialog.
	 *
	 * @return	the component to be displayed in the Preferences dialog.
	 */
	public Component getPanelComponent()
	{
		return _myPanel;
	}

	/**
	 * User has pressed OK or Apply in the dialog so save data from
	 * panel.
	 */
	public void applyChanges()
	{
		_myPanel.applyChanges(_prefs);
	}

	/**
	 * Return the title for this panel.
	 *
	 * @return	the title for this panel.
	 */
	public String getTitle()
	{
		return s_stringMgr.getString("syntax.prefSyntax");
	}

	/**
	 * Return the hint for this panel.
	 *
	 * @return	the hint for this panel.
	 */
	public String getHint()
	{
		return s_stringMgr.getString("syntax.prefSyntaxHint");
	}

	/**
	 * Component to be displayed in the preferences dialog.
	 */
	private final static class MyPanel extends JPanel
	{
      private final JRadioButton _rsyntaxActiveOpt  = new JRadioButton(s_stringMgr.getString("syntax.prefUseRsyntax"));
      private final JRadioButton _plainActiveOpt  = new JRadioButton(s_stringMgr.getString("syntax.prefUsePlain"));

      private final JCheckBox _chkTextLimitLineVisible = new JCheckBox(s_stringMgr.getString("syntax.textLimitLineVisible"));
      private final JTextField _txtTextLimitLineWidth = new JTextField();
      private final JCheckBox _chkHighlightCurrentLine = new JCheckBox(s_stringMgr.getString("syntax.highlightCurrentLine"));
      private final JCheckBox _chkLineNumbersEnabled = new JCheckBox(s_stringMgr.getString("syntax.lineNumbersEnabled"));
      
      private final JCheckBox _useCopyAsRtf = new JCheckBox(s_stringMgr.getString("syntax.useCopyAsRtf"));

		private final JTextField _txtTabLength = new JTextField();
		private final JCheckBox _chkReplaceTabsBySpaces = new JCheckBox(s_stringMgr.getString("syntax.replaceTabsBySpaces"));


		private final AdjustCaretColorCtrl _adjustCaretColorCtrl = new AdjustCaretColorCtrl();



		private StylesListSelectionListener _listLis;


		private final StylesList _stylesList = new StylesList();

		private StyleMaintenancePanel _styleMaintPnl;

		MyPanel(SyntaxPreferences prefs, SyntaxPluginResources rsrc)
		{
			super();
			createUserInterface(prefs, rsrc);
		}

		/**
		 * Component has been added to its parent so setup listeners etc.
		 */
		public void addNotify()
		{
			super.addNotify();

			if (_listLis == null)
			{
				_listLis = new StylesListSelectionListener();
				_stylesList.addListSelectionListener(_listLis);
			}
		}

		/**
		 * Component has been removed from its parent so remove listeners etc.
		 */
		public void removeNotify()
		{
			super.removeNotify();
			if (_listLis != null)
			{
				_stylesList.removeListSelectionListener(_listLis);
				_listLis = null;
			}
		}

		void loadData(SyntaxPreferences prefs)
		{
         _rsyntaxActiveOpt.setSelected(prefs.getUseRSyntaxTextArea());
         _plainActiveOpt.setSelected(prefs.getUsePlainTextControl());

         _chkTextLimitLineVisible.setSelected(prefs.isTextLimitLineVisible());
         _chkHighlightCurrentLine.setSelected(prefs.isHighlightCurrentLine());
         _chkLineNumbersEnabled.setSelected(prefs.isLineNumbersEnabled());

         _txtTextLimitLineWidth.setText("" + prefs.getTextLimitLineWidth());

         _useCopyAsRtf.setSelected(prefs.isUseCopyAsRtf());
         
         _stylesList.loadData(prefs);
			_styleMaintPnl.setStyle(_stylesList.getSelectedSyntaxStyle());

			_txtTabLength.setText("" + prefs.getTabLength());
			_chkReplaceTabsBySpaces.setSelected(prefs.isReplaceTabsBySpaces());

			updateControlStatus();


			_adjustCaretColorCtrl.loadData(prefs);
		}


      void applyChanges(SyntaxPreferences prefs)
		{
         boolean oldUseRSyntaxTextArea = prefs.getUseRSyntaxTextArea();
         boolean oldUsePlainTextControl = prefs.getUsePlainTextControl();

         try
         {
            prefs.setUseRSyntaxTextArea(_rsyntaxActiveOpt.isSelected());
            prefs.setUsePlainTextControl(_plainActiveOpt.isSelected());
         }
         catch (SyntaxPrefChangeNotSupportedException e)
         {
            prefs.setUseRSyntaxTextArea(oldUseRSyntaxTextArea);
            prefs.setUsePlainTextControl(oldUsePlainTextControl);
         }

         prefs.setTextLimitLineVisible(_chkTextLimitLineVisible.isSelected());
         prefs.setHighlightCurrentLine(_chkHighlightCurrentLine.isSelected());
         prefs.setLineNumbersEnabled(_chkLineNumbersEnabled.isSelected());
         prefs.setUseCopyAsRtf(_useCopyAsRtf.isSelected());

			fillTextLineLimit(prefs);

			fillTabLength(prefs);

			prefs.setReplaceTabsBySpaces(_chkReplaceTabsBySpaces.isSelected());


         prefs.setColumnStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.COLUMNS));
			prefs.setCommentStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.COMMENTS));
			prefs.setErrorStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.ERRORS));
			prefs.setFunctionStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.FUNCTIONS));
			prefs.setIdentifierStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.IDENTIFIERS));
			prefs.setLiteralStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.LITERALS));
			prefs.setOperatorStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.OPERATORS));
			prefs.setReservedWordStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.RESERVED_WORDS));
			prefs.setSeparatorStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.SEPARATORS));
			prefs.setTableStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.TABLES));
			prefs.setWhiteSpaceStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.WHITE_SPACE));
			prefs.setDataTypeStyle(_stylesList.getSyntaxStyleAt(StylesList.IStylesListIndices.DATA_TYPES));

			_adjustCaretColorCtrl.applyChanges(prefs);
		}

		private void fillTabLength(SyntaxPreferences prefs)
		{
			int limit = prefs.getTabLength();
			try
			{
				int buf = Integer.parseInt(_txtTabLength.getText());

				if(0 < buf && buf < 1000)
				{
					limit = buf;
				}
				else
				{
					s_log.error("Invalid text limit widht: " + _txtTabLength.getText());
				}
			}
			catch (NumberFormatException e)
			{
				s_log.error("Invalid text limit widht: " + _txtTabLength.getText(), e);
			}
			prefs.setTabLength(limit);
		}

		private void fillTextLineLimit(SyntaxPreferences prefs)
		{
			int limit = prefs.getTextLimitLineWidth();
			try
         {
            int buf = Integer.parseInt(_txtTextLimitLineWidth.getText());

            if(0 < buf && buf < 1000)
            {
               limit = buf;
            }
            else
            {
               s_log.error("Invalid text limit widht: " + _txtTextLimitLineWidth.getText());
            }
         }
         catch (NumberFormatException e)
         {
            s_log.error("Invalid text limit widht: " + _txtTextLimitLineWidth.getText(), e);
         }
			prefs.setTextLimitLineWidth(limit);
		}

		private void updateControlStatus()
      {
    	  final boolean useRSyntaxControl = _rsyntaxActiveOpt.isSelected();
    	  final boolean usePlainControl = _plainActiveOpt.isSelected();

    	  _stylesList.setEnabled(useRSyntaxControl);
    	  _styleMaintPnl.setEnabled(useRSyntaxControl);

    	  _chkTextLimitLineVisible.setEnabled(useRSyntaxControl);
    	  _txtTextLimitLineWidth.setEnabled(useRSyntaxControl);

    	  if(useRSyntaxControl)
    	  {
    		  _txtTextLimitLineWidth.setEnabled(_chkTextLimitLineVisible.isSelected());
    	  }

    	  _chkHighlightCurrentLine.setEnabled(useRSyntaxControl);
    	  _chkLineNumbersEnabled.setEnabled(useRSyntaxControl);
    	  _useCopyAsRtf.setEnabled(useRSyntaxControl);
      }

		private void createUserInterface(SyntaxPreferences prefs, SyntaxPluginResources rsrc)
		{
			setLayout(new GridBagLayout());
			GridBagConstraints gbc;

         ButtonGroup bg = new ButtonGroup();
         bg.add(_rsyntaxActiveOpt);
         bg.add(_plainActiveOpt);


         _rsyntaxActiveOpt.addChangeListener(evt -> updateControlStatus());

         _plainActiveOpt.addChangeListener(evt -> updateControlStatus());

         _chkTextLimitLineVisible.addActionListener(e -> updateControlStatus());

         _chkHighlightCurrentLine.addActionListener(e -> updateControlStatus());

         _chkLineNumbersEnabled.addActionListener(e -> updateControlStatus());



			// i18n[syntax.osterExplain=Note: The preferable editor is the Netbeans editor. The Netbeans editor\n
			//- is less memory consuming,\n
			//- its highlightning is more exact,\n
			//- can handle many lines well.\n
			// The Oster editor is still there
			// because it can handle
			// very long lines better than the
			// Netbeans editor.
			// This is due to a known bug in
			// the Netbeans editor (Issue #41241).
			// As soon as this bug is fixed
			// the Oster editor will be removed.]
			String text = s_stringMgr.getString("syntax.osterExplain");
         gbc = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0);
         add(new MultipleLineLabel(text), gbc);

         gbc = new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5,0,5,5), 0,0);
         add(createOptionsPanel(), gbc);

         gbc = new GridBagConstraints(1,0,1,2,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
			add(createStylePanel(rsrc), gbc);

         gbc = new GridBagConstraints(0,2,2,1,1,1,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0);
			add(new JPanel(), gbc);

		}


		private JPanel createOptionsPanel()
      {
         JPanel pnlRet = new JPanel(new GridBagLayout());

         GridBagConstraints gbc;

         gbc = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(_rsyntaxActiveOpt, gbc);

         gbc = new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,5,5,5), 0,0);
         pnlRet.add(_plainActiveOpt, gbc);


         gbc = new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(40,5,0,5), 0,0);
         pnlRet.add(createPnlLineLimit(), gbc);

         gbc = new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(_chkHighlightCurrentLine, gbc);

         gbc = new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(_chkLineNumbersEnabled, gbc);

         gbc = new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(_useCopyAsRtf, gbc);

         gbc = new GridBagConstraints(0,6,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(createTabConfigPanel(), gbc);

         gbc = new GridBagConstraints(0,7,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
         pnlRet.add(_adjustCaretColorCtrl.createCaretColorPanel(), gbc);

         return pnlRet;
      }

		private JPanel createTabConfigPanel()
		{
			JPanel ret = new JPanel(new GridBagLayout());

			GridBagConstraints gbc;
			gbc = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
			ret.add(new JLabel(s_stringMgr.getString("syntax.tabLength")), gbc);

			gbc = new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
			_txtTabLength.setColumns(3);
			ret.add(_txtTabLength, gbc);

			gbc = new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0);
			ret.add(_chkReplaceTabsBySpaces, gbc);

			return ret;
		}


		private JPanel createPnlLineLimit()
      {
         GridBagConstraints gbc;
         JPanel pnlLineLimit = new JPanel(new GridBagLayout());
         gbc = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,5), 0,0);
         pnlLineLimit.add(_chkTextLimitLineVisible, gbc);

         gbc = new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,5,5,5), 0,0);
         pnlLineLimit.add(new JLabel(s_stringMgr.getString("syntax.textLimitLineWidth")), gbc);

         _txtTextLimitLineWidth.setColumns(3);
         gbc = new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,5), 0,0);
         pnlLineLimit.add(_txtTextLimitLineWidth, gbc);
         return pnlLineLimit;
      }


      private JPanel createStylePanel(SyntaxPluginResources rsrc)
      {
         JPanel pnl = new JPanel(new BorderLayout());
         // i18n[syntax.styles=Syntax Styles]
         pnl.setBorder(BorderFactory.createTitledBorder(s_stringMgr.getString("syntax.styles")));

         _styleMaintPnl = new StyleMaintenancePanel(_stylesList, rsrc);

         pnl.add(_styleMaintPnl, BorderLayout.NORTH);
         pnl.add(_stylesList, BorderLayout.CENTER);

         return pnl;
      }


		/**
		 * Selection listener for the Styles List. As selection changes in the
		 * list then update the maintenance panel to reflect the current
		 * selected style.
		 */
		private class StylesListSelectionListener implements ListSelectionListener
		{
			public void valueChanged(ListSelectionEvent evt)
			{
				_styleMaintPnl.setStyle(((StylesList)evt.getSource()).getSelectedSyntaxStyle());
			}
		}
	}
}
