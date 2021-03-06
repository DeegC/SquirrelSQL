package net.sourceforge.squirrel_sql.client.gui.session;
/*
 * Copyright (C) 2001-2004 Colin Bell
 * colbell@users.sourceforge.net
 *
 * Modifications Copyright (C) 2003-2004 Jason Height
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.action.ActionCollection;
import net.sourceforge.squirrel_sql.client.gui.session.rowcolumnlabel.RowColumnLabel;
import net.sourceforge.squirrel_sql.client.gui.titlefilepath.TitleFilePathHandler;
import net.sourceforge.squirrel_sql.client.session.IObjectTreeAPI;
import net.sourceforge.squirrel_sql.client.session.ISQLEntryPanel;
import net.sourceforge.squirrel_sql.client.session.ISQLPanelAPI;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.action.*;
import net.sourceforge.squirrel_sql.client.session.action.file.FileAppendAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileCloseAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileDetachAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileNewAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileOpenAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileOpenRecentAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FilePrintAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileReloadAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileSaveAction;
import net.sourceforge.squirrel_sql.client.session.action.file.FileSaveAsAction;
import net.sourceforge.squirrel_sql.client.session.filemanager.IFileEditorAPI;
import net.sourceforge.squirrel_sql.client.session.mainpanel.IMainPanelTab;
import net.sourceforge.squirrel_sql.client.session.mainpanel.SQLPanel;
import net.sourceforge.squirrel_sql.client.session.mainpanel.changetrack.ChangeTrackTypeChooser;
import net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.IObjectTreeListener;
import net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.ObjectTreeNode;
import net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.ObjectTreePanel;
import net.sourceforge.squirrel_sql.client.session.mainpanel.sqltab.AdditionalSQLTab;
import net.sourceforge.squirrel_sql.client.session.mainpanel.sqltab.SQLTab;
import net.sourceforge.squirrel_sql.client.session.properties.SessionProperties;
import net.sourceforge.squirrel_sql.client.session.schemainfo.FilterMatcher;
import net.sourceforge.squirrel_sql.fw.gui.GUIUtils;
import net.sourceforge.squirrel_sql.fw.gui.IToggleAction;
import net.sourceforge.squirrel_sql.fw.gui.StatusBar;
import net.sourceforge.squirrel_sql.fw.gui.ToolBar;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.Vector;

public class SessionPanel extends JPanel
{
	private final IApplication _app;

	/** ID of the session for this window. */
	private IIdentifier _sessionId;

	private PropertyChangeListener _propsListener;

	private MainPanel _mainPanel;

	/** Toolbar for window. */
	private SessionPanelToolBar _toolBar;

	private Vector<ToolbarItem> _externallyAddedToolbarActionsAndSeparators = new Vector<ToolbarItem>();

	private StatusBar _statusBar = new StatusBar();
	private boolean _hasBeenVisible;

	private transient ObjectTreeSelectionListener _objTreeSelectionLis = null;
	private TitleFilePathHandler _titleFileHandler = null;

	public SessionPanel(ISession session, TitleFilePathHandler titleFileHandler)
	{
		super(new BorderLayout());

		_titleFileHandler = titleFileHandler;

		_app = session.getApplication();
		_sessionId = session.getIdentifier();

		SessionColoringUtil.colorStatusbar(session, _statusBar);
	}

   protected void initialize(ISession session)
	{
      createGUI(session);
		propertiesHaveChanged(null);

		_propsListener = evt -> propertiesHaveChanged(evt.getPropertyName());

		session.getProperties().addPropertyChangeListener(_propsListener);   	
   }
   
   public void addToToolsPopUp(String selectionString, Action action)
   {
      getMainSQLPaneAPI().addToToolsPopUp(selectionString, action);
   }


	public void addNotify()
	{
		super.addNotify();
		if (!_hasBeenVisible)
		{
			_hasBeenVisible = true;
			_mainPanel.getObjectTreePanel().refreshTree();
		}
	}

	public ISession getSession()
	{
		return _app.getSessionManager().getSession(_sessionId);
	}

	public void sessionHasClosed()
	{
		if (_objTreeSelectionLis != null)
		{
			getObjectTreePanel().removeTreeSelectionListener(_objTreeSelectionLis);
			_objTreeSelectionLis = null;
		}

		final ISession session = getSession();
		if (session != null)
		{
			if (_propsListener != null)
			{
				session.getProperties().removePropertyChangeListener(_propsListener);
				_propsListener = null;
			}
			_mainPanel.sessionClosing(session);
			_sessionId = null;
		}
	}


	public void setStatusBarMessage(final String msg)
	{
		GUIUtils.processOnSwingEventThread(() -> _statusBar.setText(msg));
	}

   public void setStatusBarProgress(final String msg, final int minimum, final int maximum, final int value)
   {
      GUIUtils.processOnSwingEventThread(() -> _statusBar.setStatusBarProgress(msg, minimum, maximum, value));
   }

   public void setStatusBarProgressFinished()
   {
      GUIUtils.processOnSwingEventThread(() -> _statusBar.setStatusBarProgressFinished());
   }

	/**
	 * Add the passed action to the session toolbar.
	 *
	 * @param	action	Action to be added.
	 */
	public synchronized void addToToolbar(Action action)
	{
		_externallyAddedToolbarActionsAndSeparators.add(new ToolbarItem(action));
		if (null != _toolBar)
		{
			_toolBar.add(action);
		}
	}

   public synchronized void addSeparatorToToolbar()
   {
      _externallyAddedToolbarActionsAndSeparators.add(new ToolbarItem());
      if (null != _toolBar)
      {
         _toolBar.addSeparator();
      }
   }


	/**
	 * Add component to the session sheets status bar.
	 *
	 * @param	comp	Component to add.
	 */
	public void addToStatusBar(JComponent comp)
	{
		_statusBar.addJComponent(comp);
	}

	/**
	 * Remove component from the session sheets status bar.
	 *
	 * @param	comp	Component to remove.
	 */
	public void removeFromStatusBar(JComponent comp)
	{
		_statusBar.remove(comp);
	}

	private void propertiesHaveChanged(String propertyName)
	{
		final ISession session = getSession();
		final SessionProperties props = session.getProperties();
		if (propertyName == null
				|| propertyName.equals(
				SessionProperties.IPropertyNames.COMMIT_ON_CLOSING_CONNECTION))
		{
			_app.getThreadPool().addTask(new Runnable()
			{
				public void run()
				{
					session.getSQLConnection().setCommitOnClose(
							props.getCommitOnClosingConnection());
				}
			});
		}
		if (propertyName == null
				|| propertyName.equals(
				SessionProperties.IPropertyNames.SHOW_TOOL_BAR))
		{
			synchronized (this)
			{
				boolean show = props.getShowToolBar();
				if (show != (_toolBar != null))
				{
					if (show)
					{
						if (_toolBar == null)
						{
							_toolBar = new SessionPanelToolBar(session);
							for (int i = 0; i < _externallyAddedToolbarActionsAndSeparators.size(); i++)
							{
								ToolbarItem toolbarItem = _externallyAddedToolbarActionsAndSeparators.get(i);

								if (toolbarItem.isSeparator())
								{
									_toolBar.addSeparator();
								}
								else
								{
									_toolBar.add(toolbarItem.getAction());
								}
							}
							add(_toolBar, BorderLayout.NORTH);
						}
					}
					else
					{
						if (_toolBar != null)
						{
							remove(_toolBar);
							_toolBar = null;
						}
					}
				}
			}
		}
	}

	private void createGUI(ISession session)
	{
		final IApplication app = session.getApplication();

		_mainPanel = new MainPanel(session, _titleFileHandler);

		add(_mainPanel, BorderLayout.CENTER);

		Font fn = app.getFontInfoStore().getStatusBarFontInfo().createFont();
		_statusBar.setFont(fn);
		add(_statusBar, BorderLayout.SOUTH);

		_objTreeSelectionLis = new ObjectTreeSelectionListener();
		getObjectTreePanel().addTreeSelectionListener(_objTreeSelectionLis);

		addToStatusBar(new SchemaPanel(session));
		addToStatusBar(new RowColumnLabel(_mainPanel));
		validate();
	}


   public boolean isObjectTreeTabSelected()
   {
      return MainPanel.ITabIndexes.OBJECT_TREE_TAB == _mainPanel.getSelectedMainTabIndex();
   }


	public int getTabCount()
	{
		return _mainPanel.getMainTabCount();
	}

	public int getMainPanelTabIndex(IMainPanelTab mainPanelTab)
   {
      return _mainPanel.getTabIndex(mainPanelTab);
   }

	public String getSelectedCatalogFromCatalogsComboBox()
	{
		if(null == _toolBar)
		{
			return null;
		}

		return _toolBar._catalogsPanel.getSelectedCatalog();
	}

	public IMainPanelTab getMainPanelTabAt(int tabIndex)
	{
		return _mainPanel.getMainPanelTabAt(tabIndex);
	}

	public ISQLPanelAPI getMainSQLPaneAPI()
	{
		return _mainPanel.getMainSQLPanel().getSQLPanelAPI();
	}

	public ISQLPanelAPI getSelectedOrMainSQLPanelAPI()
	{
		return _mainPanel.getSelectedOrMainSQLPanel().getSQLPanelAPI();
	}


	public ISQLEntryPanel getMainSQLEntryPanel()
	{
		return getMainSQLPanel().getSQLEntryPanel();
	}

	public SQLPanel getMainSQLPanel()
	{
		return _mainPanel.getMainSQLPanel();
	}

	public java.util.List<SQLPanel> getAllSQLPanels()
	{
		return _mainPanel.getAllSQLPanels();
	}

	public SQLPanel getSelectedSQLPanel()
	{
		return _mainPanel.getSelectedSQLPanel();
	}

	public SQLPanel getSelectedOrMainSQLPanel()
	{
		return _mainPanel.getSelectedOrMainSQLPanel();
	}


	public boolean isAnSQLTabSelected()
	{
		return _mainPanel.getSelectedMainTab() instanceof SQLTab || _mainPanel.getSelectedMainTab() instanceof AdditionalSQLTab;
	}

	public void sessionWindowClosing()
	{
		_mainPanel.sessionWindowClosing();
	}


	public ObjectTreePanel getObjectTreePanel()
	{
		return _mainPanel.getObjectTreePanel();
	}

	public void selectMainTab(int tabIndex)
	{
		if (tabIndex >= _mainPanel.getMainTabCount())
		{
			throw new IllegalArgumentException("" + tabIndex + " is not a valid index into the main tabbed pane.");
		}
		if (_mainPanel.getSelectedMainTabIndex() != tabIndex)
		{
			_mainPanel.selectMainTab(tabIndex);
		}
	}

	public void selectMainTab(IMainPanelTab mainPanelTab)
	{
		int mainTabIndex = getMainPanelTabIndex(mainPanelTab);

		if(-1 == mainTabIndex)
		{
			throw new IllegalStateException("Couldn't find index for IMainPanelTab: " + mainPanelTab);
		}


		selectMainTab(mainTabIndex);
	}


	public int getSelectedMainTabIndex()
	{
		return _mainPanel.getSelectedMainTabIndex();
	}

	public IMainPanelTab getSelectedMainTab()
	{
		return _mainPanel.getSelectedMainTab();
	}



	public int addMainTab(IMainPanelTab tab)
	{
		if (tab == null)
		{
			throw new IllegalArgumentException("IMainPanelTab == null");
		}
		return _mainPanel.addMainPanelTab(tab);
	}

	public void insertMainTab(IMainPanelTab tab, int idx)
	{
		insertMainTab(tab, idx, true);
	}

	public void insertMainTab(IMainPanelTab tab, int idx, boolean selectInsertedTab)
	{
		if (tab == null)
		{
			throw new IllegalArgumentException("Null IMainPanelTab passed");
		}
		if(idx == MainPanel.ITabIndexes.SQL_TAB || idx == MainPanel.ITabIndexes.OBJECT_TREE_TAB)
		{
			throw new IllegalArgumentException("Index " + idx + "conflicts with standard tabs");
		}

		_mainPanel.insertMainPanelTab(tab, idx, selectInsertedTab);
	}

	public int removeMainTab(IMainPanelTab tab)
	{
		if (tab == null)
		{
			throw new IllegalArgumentException("Null IMainPanelTab passed");
		}
		return _mainPanel.removeMainPanelTab(tab);
	}

	public IFileEditorAPI getActiveIFileEditorAPIOrNull()
	{
		return getSelectedMainTab().getActiveFileEditorAPIOrNull();
	}

	public void performStateChanged()
	{
		_mainPanel.performStateChanged();
	}

	private class SessionPanelToolBar extends ToolBar
   {
      private IObjectTreeListener _lis;
      private CatalogsPanel _catalogsPanel;

      SessionPanelToolBar(final ISession session)
      {
         super();
         createGUI(session);
			SessionColoringUtil.colorToolbar(session, this);
      }

      public void addNotify()
      {
         super.addNotify();
         if (!_hasBeenVisible)
         {
            _hasBeenVisible = true;
            _mainPanel.getObjectTreePanel().refreshTree();
         }
      }

      public void removeNotify()
      {
         super.removeNotify();
         if (_lis != null)
         {
            getObjectTreePanel().removeObjectTreeListener(_lis);
            _lis = null;
         }
      }

      private void createGUI(ISession session)
      {
         _catalogsPanel = new CatalogsPanel(session, this);
         _catalogsPanel.addActionListener(new CatalogsComboListener());


         add(_catalogsPanel);
         ActionCollection actions = session.getApplication().getActionCollection();
         setUseRolloverButtons(true);
         setFloatable(false);
         add(actions.get(SessionPropertiesAction.class));
         add(actions.get(RefreshSchemaInfoAction.class));
         addSeparator();
         add(actions.get(ExecuteSqlAction.class));
			addSeparator();
         add(actions.get(ExecuteAllSqlsAction.class));
         addSeparator();
//			actions.get(ExecuteSqlAction.class).setEnabled(false);
         add(actions.get(SQLFilterAction.class));
//			actions.get(SQLFilterAction.class).setEnabled(false);
         addSeparator();
         add(actions.get(FileNewAction.class));
         add(actions.get(FileDetachAction.class));
         add(actions.get(FileOpenAction.class));
         add(actions.get(FileOpenRecentAction.class));
         add(actions.get(FileAppendAction.class));
         add(actions.get(FileSaveAction.class));
         add(actions.get(FileSaveAsAction.class));
         add(actions.get(FileCloseAction.class));
         add(actions.get(FilePrintAction.class));
         add(actions.get(FileReloadAction.class));

			add(new ChangeTrackTypeChooser((ChangeTrackAction) actions.get(ChangeTrackAction.class), session).getComponent());

         addSeparator();
         add(actions.get(PreviousSqlAction.class));
         add(actions.get(NextSqlAction.class));
         add(actions.get(SelectSqlAction.class));
         add(actions.get(GoToLastEditLocationAction.class));
			addSeparator();
         addToggleAction((IToggleAction) actions.get(ToggleObjectTreeBesidesEditorAction.class), session);
      }
   }

	private final class CatalogsComboListener implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			String selectedCatalog = SessionPanel.this._toolBar._catalogsPanel.getSelectedCatalog();
			if (selectedCatalog != null)
			{
				try
				{
               ISession session = getSession();
					session.getSQLConnection().setCatalog(selectedCatalog);
					refreshSchemaInBackground();
				}
				catch (SQLException ex)
				{
					getSession().showErrorMessage(ex);
					SessionPanel.this._toolBar._catalogsPanel.refreshCatalogs();
				}
			}
		}
        
		private void refreshSchemaInBackground()
		{
			final ISession session = getSession();
			session.getApplication().getThreadPool().addTask(new Runnable()
			{
				public void run()
				{
					session.getSchemaInfo().reloadAll();
					expandTreeInForeground();
				}
			});
		}
		
		private void expandTreeInForeground() {
			
			final ISession session = getSession();
			final String selectedCatalog = SessionPanel.this._toolBar._catalogsPanel.getSelectedCatalog();
			
			GUIUtils.processOnSwingEventThread(new Runnable() {
				public void run() {
					expandTablesForCatalog(session, selectedCatalog);
				}
			});
		}
		
		
      /**
		 * Since the catalog has changed, it is necessary to reload the schema info and expand the tables node
		 * in the tree. Saves the user a few clicks.
		 * 
		 * @param session
		 *           the session whose ObjectTreePanel should be updated
		 * @param selectedCatalog
		 *           the catalog that was selected.
		 */
		private void expandTablesForCatalog(ISession session, String selectedCatalog)
		{
			IObjectTreeAPI api = session.getObjectTreeAPIOfActiveSessionWindow();
			api.refreshTree(true);
			if (api.selectInObjectTree(selectedCatalog, null, new FilterMatcher("TABLE", null)))
			{
				ObjectTreeNode[] nodes = api.getSelectedNodes();

				if (nodes.length > 0)
				{
					ObjectTreeNode tableNode = nodes[0];

					// send a tree expansion event to the object tree
					api.expandNode(tableNode);
				}
			}
		}
	}


	private final class ObjectTreeSelectionListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent evt)
		{
			final TreePath selPath = evt.getNewLeadSelectionPath();
			if (selPath != null)
			{
				StringBuffer buf = new StringBuffer();
				Object[] fullPath = selPath.getPath();
				for (int i = 0; i < fullPath.length; ++i)
				{
					if (fullPath[i] instanceof ObjectTreeNode)
					{
						ObjectTreeNode node = (ObjectTreeNode)fullPath[i];
						buf.append('/').append(node.toString());
					}
				}
				setStatusBarMessage(buf.toString());
			}
		}
	}
}
