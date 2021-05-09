package app.util;/* The MIT License (MIT)
 * Copyright (c) 2012 Carl Eriksson
 *
 * Permission is hereby granted, free of charge, to any person obtaininga
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction,including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import app.c2.model.App;

import java.lang.reflect.Field;
import java.util.*;
import java.lang.StringBuilder;

public class PrintTable<T> {


    public static void main(String [] arg){
        List<App> a= new ArrayList<App>();
        a.add(new App());
        a.add(new App());

        List<String> headers= new ArrayList<String>();
        headers.add("projectId");
        new PrintTable(a, null,null);
    }

    public static Map<String, Object> parameters(Object obj) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }

    public PrintTable(List<T> objects){
        this(objects ,null, null);
    }
    public PrintTable(List<T> objects, List<String> cols){
        this( objects,  cols, null);
    }

    public PrintTable(List<T> objects, List<String> cols, String title){
        this.title = title;
        List<String> headers = new ArrayList<String>();
        List<String> keys = new ArrayList<String>();
        List<List<String>> content = new ArrayList<>();
        if(cols!=null){
            headers = cols;
        }else{
            if(objects.size()>0){
                Map<String, Object> map =  parameters(objects.get(0));
                keys.addAll(map.keySet());
                List<String> finalHeaders1 = headers;
                keys.forEach(col->{
                    if(cols==null || ( cols!=null && cols.contains(col))){
                        finalHeaders1.add(col);
                    }
                });
            }
        }
        List<String> finalHeaders = headers;
        objects.forEach(o->{
                Map<String, Object> map =  parameters(o);
            List<String> row = new ArrayList<String>();
                    finalHeaders.forEach(col->{
                    if(map.get(col)!=null){
                        row.add(map.get(col).toString());
                    }else{
                        row.add("");
                    }
                });
                content.add(row);
            });
        this.headers = headers;
        this.maxLength =  new ArrayList<Integer>();
        //Set headers length to maxLength at first
        for(int i = 0; i < headers.size(); i++){
            maxLength.add(headers.get(i).length());
        }
        this.table = content;
        calcMaxLengthAll();
        printTable();
    }
    /*
     * Modify these to suit your use
     */
    private final int TABLEPADDING = 0;
    private final char SEPERATOR_CHAR = '-';

    private List<String> headers;
    private List<List<String>> table;
    private List<Integer> maxLength;
    private int rows,cols;
    private String title;

    /*
     * To update the matrix
     */
    public void updateField(int row, int col, String input){
        //Update the value
        table.get(row).set(col,input);
        //Then calculate the max length of the column
        calcMaxLengthCol(col);
    }
    /*
     * Prints the content in table to console
     */
    private void printTable(){
        //Take out the
        StringBuilder sb = new StringBuilder();
        StringBuilder sbRowSep = new StringBuilder();
        String padder = "";
        int rowLength = 0;
        String rowSeperator = "";

        //Create padding string containing just containing spaces
        for(int i = 0; i < TABLEPADDING; i++){
            padder += " ";
        }
        //Create the rowSeperator
        for(int i = 0; i < maxLength.size(); i++){
            sbRowSep.append("|");
            for(int j = 0; j < maxLength.get(i)+(TABLEPADDING*2); j++){
                sbRowSep.append(SEPERATOR_CHAR);
            }
        }
        sbRowSep.append("|");
        rowSeperator = sbRowSep.toString();

        String fullLine = rowSeperator.replace("|","-");
        if(title!=null && title.length()>0){
            sb.append(fullLine);
            sb.append("\n");
            sb.append("|"+title+fullLine.substring(0,fullLine.length()-(title.length()+2)).replace("-"," ")+"|");
        }
        sb.append("\n");
        sb.append(fullLine);
        sb.append("\n");
        //HEADERS
        sb.append("|");
        for(int i = 0; i < headers.size(); i++){
            sb.append(padder);
            sb.append(headers.get(i));
            //Fill up with empty spaces
            for(int k = 0; k < (maxLength.get(i)-headers.get(i).length()); k++){
                sb.append(" ");
            }
            sb.append(padder);
            sb.append("|");
        }
        sb.append("\n");
        sb.append(fullLine);
        sb.append("\n");

        //BODY
        for(int i = 0; i < table.size(); i++){
            List<String> tempRow = table.get(i);
            //New row
            sb.append("|");
            for(int j = 0; j < tempRow.size(); j++){
                sb.append(padder);
                sb.append(tempRow.get(j));
                //Fill up with empty spaces
                for(int k = 0; k < (maxLength.get(j)-tempRow.get(j).length()); k++){
                    sb.append(" ");
                }
                sb.append(padder);
                sb.append("|");
            }
            sb.append("\n");
            sb.append(fullLine);
            sb.append("\n");
        }
        ConsoleHelper.console.display(sb.toString());
    }
    /*
     * Fills maxLenth with the length of the longest word
     * in each column
     *
     * This will only be used if the user dont send any data
     * in first init
     */
    private void calcMaxLengthAll(){
        for(int i = 0; i < table.size(); i++){
            List<String> temp = table.get(i);
            for(int j = 0; j < temp.size(); j++){
                //If the table content was longer then current maxLength - update it
                if(temp.get(j).length() > maxLength.get(j)){
                    maxLength.set(j, temp.get(j).length());
                }
            }
        }
    }
    /*
     * Same as calcMaxLength but instead its only for the column given as inparam
     */
    private void calcMaxLengthCol(int col){
        for(int i = 0; i < table.size(); i++){
            if(table.get(i).get(col).length() > maxLength.get(col)){
                maxLength.set(col, table.get(i).get(col).length());
            }
        }
    }
}