package com.example.fileexplore;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monkey D Luffy on 2017/6/12.
 */

public class SaxParseXml extends DefaultHandler {
    //存放遍历集合
    private List<WebFile> list;
    //构建Student对象
    private WebFile webFile;
    //用来存放每次遍历后的元素名称(节点名称)
    private String tagName;


    public List<WebFile> getList() {
        return list;
    }


    public void setList(List<WebFile> list) {
        this.list = list;
    }


    public WebFile getStudent() {
        return webFile;
    }


    public void setStudent(WebFile webFile) {
        this.webFile = webFile;
    }


    public String getTagName() {
        return tagName;
    }


    public void setTagName(String tagName) {
        this.tagName = tagName;
    }


    //只调用一次  初始化list集合
    @Override
    public void startDocument() throws SAXException {
        list=new ArrayList<WebFile>();

    }


    //调用多次    开始解析
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals("file")){
            webFile=new WebFile();
        }
        tagName=qName;
    }
    //调用多次
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("file")){
            this.list.add(this.webFile);
        }
        tagName=null;
    }


    //只调用一次
    @Override
    public void endDocument() throws SAXException {

    }

    //调用多次
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(tagName!=null){
            String date=new String(ch,start,length);
            if(tagName.equals("username")){
                webFile.setUsername(date);
            }
            else if(tagName.equals("filepath")){
                webFile.setFilepath(date);
            }
        }
    }
}
