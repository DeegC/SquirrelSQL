package net.sourceforge.squirrel_sql.client.session.action.file;

import net.sourceforge.squirrel_sql.client.action.IFileEditAction;
import net.sourceforge.squirrel_sql.client.action.SquirrelAction;
import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.session.filemanager.FileHandler;

import java.awt.event.ActionEvent;

public class FileSaveAsAction extends SquirrelAction  implements IFileEditAction
{
   private FileHandler _fileHandler;

   public FileSaveAsAction(IApplication app)
   {
      super(app);
   }

   public void actionPerformed(ActionEvent e)
   {
      _fileHandler.fileSaveAs();
   }


   @Override
   public void setFileHandler(FileHandler fileHandler)
   {
      _fileHandler = fileHandler;
      setEnabled(null != _fileHandler);
   }
}
