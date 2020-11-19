package Assignment1;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.util.*;
import java.io.File;
import java.io.BufferedReader;
import java.util.Scanner;
import java.io.StringReader;


public class Index {

	private static final String INDEX_DIR = "/home/ec2-user/Assignment1/index";
	private static final String ENTRIES_DOC_PATH = "/home/ec2-user/Assignment1/data/cran.all.1400";
	
	/*
	 * The five fields are '.I, .T, .A, .B, .W'. While a mapping to a more descriptive string
	 * is unnecessary for evaluation, it provides more clarity when viewing an index with Luke
	 */
	private static final HashMap<String, String> FIELD_MAPPINGS = Utilities.GetFieldMappings();
	
	public static void main(String[] args)
	{
		try
		{
			File entriesFile = new File(ENTRIES_DOC_PATH);
			Scanner entriesReader = new Scanner(entriesFile);
		    
			//Analyzer is initialized as either a Lucene built-in analyzer, or a token stream as described in Utilities_tokenizer.java
	        var analyzer = Utilities.USE_TOKENIZER ? new Utilities_Tokenizer() : Utilities.GetAnalyzer();
	        IndexWriter writer = Utilities.GetIndexWriter(analyzer, INDEX_DIR);
	        
	        //currentEntry is a multiline string representation of the fields of the current abstract entry
	        String currentEntry = "";
	        String currLine = "";
			while (entriesReader.hasNextLine())
			{
				currLine = entriesReader.nextLine();
				//Reaching a new .I field indicates the end of the previous abstract (except for the initial .I, in which case currentEntry is empty)
				if (IsNewField(currLine) && ReturnNewField(currLine).equals(Utilities.INDEX_FIELD) && !currentEntry.isEmpty())
				{
					IndexDoc(currentEntry, writer);
					currentEntry = currLine;
				}
				else
				{
					currentEntry += "\n" + currLine;
				}
			}
			entriesReader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void IndexDoc(String entry, IndexWriter writer)
	{
		try {
			BufferedReader entryReader = new BufferedReader(new StringReader(entry));
			String currLine = "";
			
			//currFieldKey represents the key of the current field (eg. .I, .T etc)
			String currFieldKey = "";
			
			//currFieldEntry represents the text associated with the current field
			String currFieldEntry = "";
			
			Document doc = new Document();
			while ((currLine = entryReader.readLine()) != null)
			{
				if (IsNewField(currLine))
				{
					if (currFieldKey != "")
					{
						currFieldEntry = currFieldEntry.trim();
						
						//Create a new Text field to add to the document. 
						//The field name is mapped from the currFieldKey (eg. .T -> Title)
						Field field = new TextField(FIELD_MAPPINGS.get(currFieldKey), currFieldEntry, Field.Store.YES);
						doc.add(field);
						currFieldEntry = "";
					}
					currFieldKey = ReturnNewField(currLine);
					currFieldEntry += currLine.substring(2).trim();
				}
				else
				{
					currFieldEntry += " " + currLine.trim();
				}
			}
			currFieldEntry = currFieldEntry.trim();
			Field field = new TextField(FIELD_MAPPINGS.get(currFieldKey), currFieldEntry, Field.Store.YES);
			doc.add(field);
			entryReader.close();
			writer.addDocument(doc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static Boolean IsNewField(String line)
	{
		if (line.length() < 2)
			return false;
		String fieldSub = line.substring(0,2);
		return FIELD_MAPPINGS.containsKey(fieldSub);
	}
	
	private static String ReturnNewField(String line)
	{
		return line.substring(0, 2);
	}
}
