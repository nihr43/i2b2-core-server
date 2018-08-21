package edu.harvard.i2b2.crc.sql.parser.contains.translators;

import edu.harvard.i2b2.crc.sql.parser.contains.ParseResult;
import edu.harvard.i2b2.crc.sql.parser.contains.Token;
import edu.harvard.i2b2.crc.sql.parser.contains.TokenizedStatement;
import edu.harvard.i2b2.crc.sql.parser.contains.rules.I2b2Grammar;

public class OracleTranslator extends Translator
{
	
	public String getName()
	{ return "Oracle"; }
	
	// assumes st has already been syntax-checked and therefore POS-tagged
	public String translate( TokenizedStatement ts )
	{
		String translatedStr = "";	
		// first mark double quotes as open brace/close brace
		boolean isOpenQuote = true;
		for (int i = 0; i < ts.getTokenCount(); i++)
		{			
			Token t = ts.getTokenAt(i);
			if (t.getPOS() == Token.POS.DOUBLEQUOTE)
			{
				if (isOpenQuote)
					t.setPOS(Token.POS.OPEN_BRACE);
				else
					t.setPOS(Token.POS.CLOSE_BRACE);
				isOpenQuote = !isOpenQuote;
			}
		}
		
		// produce translated text by using POS tags
		for (int i = 0; i < ts.getTokenCount(); i++)
		{			
			Token t = ts.getTokenAt(i);
			if (t.getPOS() == Token.POS.AND)
			{
				if (i+1 >= ts.getTokenCount())
					return null; // error, not expecting end of tokens
				Token t2 = ts.getTokenAt(i+1);
				if (t2.getPOS() != Token.POS.NOT )
				{
					if (!translatedStr.endsWith(" "))
						translatedStr = translatedStr + " ";
					translatedStr = translatedStr + "and ";
				}
			}
			else if (t.getPOS() == Token.POS.OR)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "or ";
			}
			else if (t.getPOS() == Token.POS.NOT)
			{
				if (!translatedStr.endsWith(" "))
					translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "not ";
			}
			else if (t.getPOS() == Token.POS.OPEN_PARENTHESIS)
			{
				if (!translatedStr.endsWith(" "))
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_BRACE && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS)
						translatedStr = translatedStr + " ";
				translatedStr = translatedStr + "(";
			}
			else if (t.getPOS() == Token.POS.CLOSE_PARENTHESIS)
			{
				translatedStr = translatedStr + ")";
			}
			else if (t.getPOS() == Token.POS.OPEN_BRACE)
				translatedStr = translatedStr + "{";
			else if (t.getPOS() == Token.POS.CLOSE_BRACE)
				translatedStr = translatedStr + "}";
			else
			{
				if (!translatedStr.endsWith(" "))
				{
					if ((i != 0) && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_BRACE && ts.getTokenAt(i-1).getPOS()!= Token.POS.OPEN_PARENTHESIS)
						translatedStr = translatedStr + " ";
				}
				translatedStr = translatedStr + t.getString();
			}
		}
		return translatedStr;
	}
	
	public static void main( String [] args )
	{
		I2b2Grammar s = new I2b2Grammar();
		// 
		// 
		String[] strs = {
						 "(wrist)", "rheumatoid arthritis", "rheumatoid and arthritis", "rheumatoid and arthritis and heart or \"some other phrase\" or cat",
						 "\"rheumatoid arthritis\"", " \" rheumatoid arthritis \"",  "\"rheumatoid arthritis \"", "\" rheumatoid arthritis\"", "rheumatoid arthritis\"\"", "rheumatoid arthritis \"\"",
						 "rheumatoid and arthritis", "rheumatoid aNd arthritis", "\"rheumatoid\" and arthritis", "rheumatoid and \"arthritis\"", "\"rheumatoid arthritis\" and wrist", "wrist and \" rheumatoid and arthritis \"",
						 "rheumatoid and arthritis and wrist", 	"wrist and \" rheumatoid and arthritis \" and bones",
						 "rheumatoid and arthritis or RA", "Reumatoid or Arthritis or RA", "\"Reumatoid arthritis\" or RA", "A or B or C or D", "A and B or C and D", "A and B oror C", "A or B aand C",
						 " \"rheumatoid arthritis \" or \" broken wrist\"", "broken and wrist or \"rheumatoid arthritis\"",
				/* 27 */ " rheumatoid  not arthritis", "\"rheumatoid \" NOT \"arthritis\"", "\"rheumatoid\" and not", "rhematoid not \"rheumatoid arthritis\"", "rheumatoid and",
						 "(wrist)", "((wrist))", 
						 "(((wrist)))", "(wrist", "wrist)", 
						 "(\"BEEF\")", "(\"Beef noodles\")",
				/* 39*/  "((\"i r b\" not RA))",
						 "\"(\" and \"wrist \" or ((\"i r b\" not RA))",
						 "\"\" and \"(wrist)\" or \"\"", "\"\"(wrist)\"\"", "\"\"((wrist))\"\"", "()", "wris(t",  
						 "broken and (wrist or \"rheumatoid arthritis\")", "broken and (wrist or \"rheumatoid arthritis\")"
						};
		
		Translator t = new OracleTranslator();
		for (int i = 0; i < strs.length; i++ )
		{
			TokenizedStatement ts = new TokenizedStatement( strs[i] );
			System.err.println( strs[i] );
			ParseResult r = s.parse( ts );
			for (int j=0; j < ts.getTokenCount(); j++)
				System.err.println("\t [" + j + "] = " + ts.getTokenAt(j) + ": (" + ts.getTokenAt(j).getPOS() + ")");			
			System.err.println( "\tResult: " + r.toString() );
			
			System.err.println( "["+i + "] Parsing: '" + strs[i] + "'");
			System.err.println( "["+i + "] Oracle : '" + t.translate(ts) + "'");
			System.err.println( "" );
		}
	}
}
