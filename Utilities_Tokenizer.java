package Assignment1;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.CharArraySet;

import java.util.Arrays;

public class Utilities_Tokenizer extends Analyzer{
	
	// Configure Tokenizer
	private static final Tokenizers SELECTED_TOKENIZER = Tokenizers.Letter;
	
	// Configure Filter Pipeline
	private static final Filters[] FILTER_PIPELINE = new Filters[] {Filters.Stem, Filters.Stop, Filters.Length};
	
	// List of stop words recommended by Lucene
	private static String[] STOP_WORDS = new String[] {
			"a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
			"in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that",
			"the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"
	};
	
	private static enum Tokenizers {
		Standard,
		Whitespace,
		Letter,
		Classic
	}
	
	private static enum Filters {
		Classic,
		Stop,
		Length,
		Stem
	}
	
	@Override
	public Analyzer.TokenStreamComponents createComponents(String temp)
	{
		System.out.println("Tokenizer: " + (SELECTED_TOKENIZER == null ? Tokenizers.Standard.toString() : SELECTED_TOKENIZER.toString()));
		var tokenizer = SELECTED_TOKENIZER == Tokenizers.Whitespace ? new WhitespaceTokenizer() :
						SELECTED_TOKENIZER == Tokenizers.Letter ? new LetterTokenizer() :
						SELECTED_TOKENIZER == Tokenizers.Standard ? new StandardTokenizer() :
						SELECTED_TOKENIZER == Tokenizers.Classic ? new ClassicTokenizer() :
						new StandardTokenizer();
						
		TokenStream stream = tokenizer;
						
		for (Filters filter : FILTER_PIPELINE)
		{
			System.out.println("Filter: " + filter.toString());
			switch(filter)
			{
			case Classic:
				stream = new ClassicFilter(stream);
				break;
			case Stop:
				CharArraySet stopWords = new CharArraySet(STOP_WORDS.length, true);
				stopWords.addAll(Arrays.asList(STOP_WORDS));
				stream = new StopFilter(stream, stopWords);
				break;
			case Length:
				stream = new LengthFilter(stream, 3, 17);
				break;
			case Stem:
				stream = new PorterStemFilter(stream);
			}
		}			
		return new Analyzer.TokenStreamComponents(tokenizer, stream);
	}
}
