package com.example.studynotesexporter;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Действие редактора IntelliJ IDEA, экспортирующее выделенный фрагмент текста
 * или кода вместе с именем исходного файла и номером начальной строки в
 * документ {@code .docx}.
 */
public class ExportSelectionAction extends AnAction {

    /**
     * Путь к целевому {@code .docx}-файлу по умолчанию, куда дописываются
     * экспортируемые фрагменты.
     */
    private static final Path OUTPUT_PATH = Path.of("C:\\cpp\\studyfile.docx");

    /**
     * Возвращает поток, в котором обновляется состояние действия в IntelliJ Platform.
     *
     * @return {@link ActionUpdateThread#EDT} для работы с состоянием редактора и выделения
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    /**
     * Проверяет наличие активного редактора и непустого выделения, определяет имя
     * файла и номер начальной строки (с 1), запускает экспорт через {@link DocxExporter}
     * и отображает диалоговое окно с результатом или ошибкой.
     *
     * @param e контекст события действия, содержащий проект, редактор и данные окружения
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null || !editor.getSelectionModel().hasSelection()) {
            Messages.showWarningDialog(
                    project,
                    "Please select text in the editor first.",
                    "Study Notes Exporter"
            );
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            Messages.showWarningDialog(
                    project,
                    "Selected text is empty.",
                    "Study Notes Exporter"
            );
            return;
        }

        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        String fileName = (virtualFile != null) ? virtualFile.getName() : "UnknownFile";

        int selectionStartOffset = editor.getSelectionModel().getSelectionStart();
        int lineNumber = document.getLineNumber(selectionStartOffset) + 1;

        try {
            DocxExporter exporter = new DocxExporter();
            exporter.exportTextToDocx(selectedText, fileName, lineNumber, OUTPUT_PATH);

            Messages.showInfoMessage(
                    project,
                    "Selection exported to: " + OUTPUT_PATH,
                    "Study Notes Exporter"
            );
        } catch (IOException ex) {
            Messages.showErrorDialog(
                    project,
                    "Failed to export selection: " + ex.getMessage(),
                    "Study Notes Exporter"
            );
        }
    }
}