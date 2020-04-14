import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.Arrays;
import java.util.List;

/**
 * @author xingjun
 * @date 2020/4/13 9:04
 */
public class queryJump extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //1.获取方法名：
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) return;
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element != null && "PsiJavaToken:STRING_LITERAL".equals(element.toString())) {
            String selectedText = element.getText();
            selectedText = selectedText.replaceAll("\"", "");
            List<String> stringList = Arrays.asList(selectedText.split("\\."));
            if (stringList != null && stringList.size() > 1) {
                //method name
                String methodName = stringList.get(stringList.size() - 1);
                //mapper temporary name
                String mapperTempName = stringList.get(stringList.size() - 2);
                //sql mapper
                String mapperName = mapperTempName + ".xml";
                //ES mapper
                String mapperDynamicName = mapperTempName + ".dynamic.xml";
                if (getFile(anActionEvent, mapperName, methodName)) {
                    return;
                } else if (getFile(anActionEvent, mapperDynamicName, methodName)) {
                    return;
                } else {
                    //Messages.showMessageDialog("Can't find the file: " + selectedText, "Information", Messages.getInformationIcon());
                    return;
                }
            } else {
                //Messages.showMessageDialog("Your selection is not a queryId~", "Information", Messages.getInformationIcon());
                return;
            }
        } else {
            //Messages.showMessageDialog("Your selection is not a String!", "Information", Messages.getInformationIcon());
            return;
        }
    }


    private Boolean getFile(AnActionEvent anActionEvent, String mapperName, String methodName) {
        Project project = anActionEvent.getProject();
        //find the file of the name is 'mapperName'
        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(mapperName);
        if (files.length == 1) {
            XmlFile xmlFile = (XmlFile) files[0];
            XmlTag[] subTags = xmlFile.getDocument().getRootTag().getSubTags();
            for (XmlTag subTag : subTags) {
                XmlAttribute[] attributes = subTag.getAttributes();
                for (XmlAttribute attribute : attributes) {
                    if (attribute.getName().equals("id")) {
                        if (attribute.getValue().equals(methodName)) {
                            int offset = attribute.getTextOffset();
                            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, files[0].getVirtualFile(), offset);
                            FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }

    }
}