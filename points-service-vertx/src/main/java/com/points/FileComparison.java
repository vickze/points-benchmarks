package com.points;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileComparison {
  public static void main(String[] args) {
    String file1Path = "D:\\1.txt";
    String file2Path = "D:\\2.txt";

    List<String> file1List = readFile(file1Path);
    List<String> file2List = readFile(file2Path);

    List<String> jpgOnlyInFile2 = new ArrayList<>();

    for (String fileName : file1List) {
        if (!file2List.contains(fileName)) {
          jpgOnlyInFile2.add(fileName);
      }
    }

    System.out.println("List1大小：" + file1List.size());
    System.out.println("List2大小：" + file2List.size());
    System.out.println("2.txt比1.txt文件少的JPG文件有：");
    for (String fileName : jpgOnlyInFile2) {
      System.out.println(fileName);
    }
  }

  private static List<String> readFile(String filePath) {
    List<String> fileList = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.endsWith(".JPG")) {
          fileList.add(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileList;
  }
}
