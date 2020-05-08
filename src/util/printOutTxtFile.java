package util;

import java.io.BufferedWriter;
import java.io.File;

public class printOutTxtFile
{
  public printOutTxtFile() {}
  
  public static void Write(String dest, java.util.ArrayList<String> content) throws java.io.IOException {

    String[] split = dest.split("/");
    if (split.length>1) {
      int last =dest.lastIndexOf("/");
      String subs = dest.substring(0,last+1);
      File f = new File(subs);
      if (!f.exists()) f.mkdirs();
    }
    java.io.FileWriter fw = new java.io.FileWriter(dest);
    BufferedWriter bw = new BufferedWriter(fw);
    
    for (String s : content) {
      bw.write(s);
      bw.newLine();
    }
    bw.close();
    fw.close();
  }
}
