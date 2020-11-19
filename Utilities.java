package Assignment1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.*;

import java.nio.file.Paths;
import java.util.HashMap;

public class Utilities{
	
	public static final String INDEX_FIELD = ".I";
	public static final String CONTENT_FIELD = ".W";
	
	// Set to false to use Utilities_Tokenizer.java instead
	public static final Boolean USE_TOKENIZER = true;
	
	// Configure Ranking Function 
	private static final SimilarityClasses SELECTED_SIMILARITY_CLASS = SimilarityClasses.BM25;
	
	// Configure Analyzer
	private static final Analyzers SELECTED_ANALYZER = Analyzers.Classic;
	
	public static enum SimilarityClasses {
		BM25,
		VSM
	}
	
	private static enum Analyzers {
		Keyword,
		Whitespace,
		Simple,
		Classic,
		Standard
	}
	
	public static Analyzer GetAnalyzer()
	{
		System.out.println("Analyzer: " + (SELECTED_ANALYZER == null ? Analyzers.Standard.toString() : SELECTED_ANALYZER.toString()));
		switch(SELECTED_ANALYZER)
		{
		case Keyword:
			return new KeywordAnalyzer();
		case Whitespace:
			return new WhitespaceAnalyzer();
		case Simple:
			return new SimpleAnalyzer();
		case Classic:
			return new ClassicAnalyzer();
		case Standard:
		default:
			return new StandardAnalyzer();
		}
	}
	
	public static IndexSearcher GetSearcher(String indexPath)
	{
		try 
		{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
			var searcher = new IndexSearcher(reader);
			System.out.println("Ranking Function: " + (SELECTED_SIMILARITY_CLASS == null ? SimilarityClasses.VSM.toString() : SELECTED_SIMILARITY_CLASS.toString()));
			switch(SELECTED_SIMILARITY_CLASS)
			{
				case BM25:
					searcher.setSimilarity(new BM25Similarity());
					break;
				case VSM:
				default:
					searcher.setSimilarity(new ClassicSimilarity());
			}			
			return searcher;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static IndexWriter GetIndexWriter(Analyzer analyzer, String indexPath)
	{
		try
		{
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			//Creates a new index each time to test different analyzers/tokenizers/ranking functions
			iwc.setOpenMode(OpenMode.CREATE);
			System.out.println("Ranking Function: " + (SELECTED_SIMILARITY_CLASS == null ? SimilarityClasses.VSM.toString() : SELECTED_SIMILARITY_CLASS.toString()));
			switch(SELECTED_SIMILARITY_CLASS)
			{
				case BM25:
					iwc.setSimilarity(new BM25Similarity());
					break;
				case VSM:
				default:
					iwc.setSimilarity(new ClassicSimilarity());
			}			
	        IndexWriter writer = new IndexWriter(dir, iwc);
	        return writer;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static HashMap<String, String> GetFieldMappings()
	{
		var fieldMappings = new HashMap<String, String>();
		fieldMappings.put(".I", "index");
		fieldMappings.put(".T", "title");
		fieldMappings.put(".A", "author");
		fieldMappings.put(".B", "citation");
		fieldMappings.put(".W", "content");
		return fieldMappings;
	}
}
