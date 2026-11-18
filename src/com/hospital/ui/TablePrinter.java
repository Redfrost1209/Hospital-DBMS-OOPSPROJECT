package com.hospital.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility to render formatted ASCII tables in the terminal.
 */
public class TablePrinter {
    private final List<String> headers;
    private final List<List<String>> rows = new ArrayList<>();

    public TablePrinter(String... headers) {
        this.headers = Arrays.asList(headers);
    }

    public void addRow(String... rowValues) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (i < rowValues.length && rowValues[i] != null) {
                row.add(rowValues[i]);
            } else {
                row.add("");
            }
        }
        rows.add(row);
    }

    public void print() {
        if (headers.isEmpty()) return;

        int[] colWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (row.get(i).length() > colWidths[i]) {
                    colWidths[i] = row.get(i).length();
                }
            }
        }

        // Add padding
        for (int i = 0; i < colWidths.length; i++) {
            colWidths[i] += 2;
        }

        printHorizontalBorder(colWidths, '+', '-');
        printRow(headers, colWidths);
        printHorizontalBorder(colWidths, '+', '=');

        if (rows.isEmpty()) {
            System.out.println("| " + padCenter("(No records found)", getTotalWidth(colWidths) - 4) + " |");
        } else {
            for (List<String> row : rows) {
                printRow(row, colWidths);
            }
        }

        printHorizontalBorder(colWidths, '+', '-');
    }

    private void printRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            sb.append(" ").append(padRight(val, widths[i] - 1)).append("|");
        }
        System.out.println(sb);
    }

    private void printHorizontalBorder(int[] widths, char corner, char line) {
        StringBuilder sb = new StringBuilder();
        sb.append(corner);
        for (int width : widths) {
            for (int i = 0; i < width; i++) {
                sb.append(line);
            }
            sb.append(corner);
        }
        System.out.println(sb);
    }

    private int getTotalWidth(int[] widths) {
        int total = 1;
        for (int w : widths) {
            total += w + 1;
        }
        return total;
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private String padCenter(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int leftPad = (width - s.length()) / 2;
        int rightPad = width - s.length() - leftPad;
        return " ".repeat(leftPad) + s + " ".repeat(rightPad);
    }
}
