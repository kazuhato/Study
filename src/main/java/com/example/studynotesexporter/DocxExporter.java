package com.example.studynotesexporter;

import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Экспортирует выделенные в редакторе фрагменты текста в документ формата
 * Microsoft Word ({@code .docx}) с использованием библиотеки Apache POI.
 *
 * <p>Для каждого экспортируемого фрагмента добавляет строку с именем исходного
 * файла и номером начальной строки, оформляет текст шрифтом {@code Consolas}
 * с левым отступом и границами сверху/снизу блока, а в конце добавляет пустой
 * абзац-разделитель.</p>
 */
public class DocxExporter {

    /**
     * Дописывает переданный фрагмент текста в указанный {@code .docx}-файл,
     * при необходимости создавая родительские каталоги и сам документ.
     *
     * @param text       выделенный в редакторе фрагмент текста или кода
     * @param fileName   имя исходного файла
     * @param lineNumber номер начальной строки выделения (начиная с 1)
     * @param outputPath путь к целевому {@code .docx}-файлу
     * @throws IOException если при создании каталогов, чтении или записи файла произошла ошибка ввода-вывода
     */
    public void exportTextToDocx(String text, String fileName, int lineNumber, Path outputPath) throws IOException {
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        XWPFDocument document;
        if (Files.exists(outputPath)) {
            try (FileInputStream fis = new FileInputStream(outputPath.toFile())) {
                document = new XWPFDocument(fis);
            }
        } else {
            document = new XWPFDocument();
        }

        XWPFParagraph metaParagraph = document.createParagraph();
        XWPFRun metaRun = metaParagraph.createRun();
        metaRun.setText("File: " + fileName + " | Line: " + lineNumber);
        metaRun.setFontFamily("Consolas");
        metaRun.setFontSize(9);
        metaRun.setColor("666666");

        String normalizedText = text.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalizedText.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            XWPFParagraph codeParagraph = document.createParagraph();
            codeParagraph.setIndentationLeft(240);

            if (i == 0) {
                codeParagraph.setBorderTop(Borders.SINGLE);
            }
            if (i == lines.length - 1) {
                codeParagraph.setBorderBottom(Borders.SINGLE);
            }

            XWPFRun codeRun = codeParagraph.createRun();
            codeRun.setText(lines[i]);
            codeRun.setFontFamily("Consolas");
            codeRun.setFontSize(10);
            codeRun.setColor("1E1E1E");
        }

        document.createParagraph();

        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            document.write(fos);
        } finally {
            document.close();
        }
    }
}