package Assignment1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.io.PrintWriter;
import java.util.HashMap;

public class Search {

	private static final String INDEX_DIR = "/home/ec2-user/Assignment1/index";
	private static final String QUERIES_FILE = "/home/ec2-user/Assignment1/data/cran.qry";
	private static final String RESULTS_FILE = "/home/ec2-user/Assignment1/results.txt";
	
	private static final int NUMBER_OF_DOCUMENTS = 1400;
	
	// Query number - compensates for the disparity between ids in cran.qry and qrels file
	private static int queryNumber;
	
	
	 // The five fields are '.I, .T, .A, .B, .W'. While a mapping to a more descriptive string
	 // is unnecessary for evaluation, it provides more clarity when viewing an index with Luke
	private static HashMap<String, String> fieldMappings = Utilities.GetFieldMappings();
	
	public static void main(String[] args)
	{
		try
		{
			queryNumber = 1;
			
			File queryFile = new File(QUERIES_FILE);
			Scanner queryStream = new Scanner(queryFile);
			PrintWriter resultsWriter = new PrintWriter(new FileOutputStream(RESULTS_FILE, false));
			
			String currentQueryID = "";
			String currentQuery = "";
			String currentLine = "";
			
			// Analyzer is initialized as either a Lucene built-in analyzer, or a token stream as described in Utilities_tokenizer.java
			var analyzer = Utilities.USE_TOKENIZER ? new Utilities_Tokenizer() : Utilities.GetAnalyzer();
			
			var queryParser = GetQueryParser(analyzer);
		    var searcher = Utilities.GetSearcher(INDEX_DIR);
		    System.out.println("Making queries..");
			
			while(queryStream.hasNextLine())
			{
				currentLine = queryStream.nextLine();
				
				// Once a new '.I' field is reached, if it's not the first .I field in the doc, the query is processed
				if (currentLine.substring(0,2).equals(Utilities.INDEX_FIELD))
				{
					if (!currentQueryID.isEmpty())
					{
						MakeQuery(resultsWriter, queryParser, searcher, queryNumber++, currentQuery);
						currentQuery = "";
					}
					currentQueryID = currentLine.substring(2).trim();
				}
				else if (!currentLine.substring(0,2).equals(Utilities.CONTENT_FIELD))
				{
					currentQuery += " " + currentLine;
				}
			}
			MakeQuery(resultsWriter, queryParser, searcher, queryNumber, currentQuery);
			queryStream.close();
			resultsWriter.close();
			System.out.println("Finished.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	private static void MakeQuery(PrintWriter writer, QueryParser parser, IndexSearcher searcher, int queryID, String queryString)
	{
		try
		{
			// Lucene searches require a parsed Query object
			var query = parser.parse(queryString);
			
			// hits represents an ordered (by relevance) array of search results.
			// It searches for 1400 documents (total no. of documents) so returns documents with any relevance
			ScoreDoc[] hits = searcher.search(query, NUMBER_OF_DOCUMENTS).scoreDocs;
			
			for (int i = 0; i < hits.length-1; i++)
			{
				Document hitDoc = searcher.doc(hits[i].doc);
				
				//QueryID null null DocumentID Rank Score null
				writer.println(queryID + " 0 " + hitDoc.get("index") + " " + i + " " + hits[i].score + " 0");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static MultiFieldQueryParser GetQueryParser(Analyzer analyzer)
	{
		// MultiFieldQueryParser allows for a query to search all the fields of a document
		String[] fieldsToAnalyze = fieldMappings.values().toArray(new String[0]);
		var parser = new MultiFieldQueryParser(fieldsToAnalyze, analyzer);
		
		// Some queries have leading wildcards (eg. ?test)
		parser.setAllowLeadingWildcard(true);
		
		return parser;
	}
}
