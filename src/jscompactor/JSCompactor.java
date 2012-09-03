package jscompactor;
import java.io.FileInputStream;
import java.io.File;
public class JSCompactor
{
	static int TAB_SIZE = 4;
    static int LINE_LEN = 60;
    static void chunkify( String text )   
    {
        System.out.print( "\"" );
        boolean pending = false;
        for ( int i=0;i<text.length();i++ )
        {
            if ( i % LINE_LEN == 0 && i > 0 )
                pending = true;
            System.out.print(text.charAt(i));
            if ( pending )
            {
                if ( text.charAt(i)!='\\')
                {
                    System.out.print("\"\n+\"" );
                    pending = false;
                }
            }
        }
        System.out.println("\"");
    }
    private static void append( StringBuilder sb, char token )
    {
        if ( token == '"' )
            sb.append("\\\"");
        else if ( token == '\\' )
            sb.append("\\\\");
        else if ( token == '\t' )
            sb.append("\\t");
        else               
            sb.append( token );
    }
	public static void main(String[] args )
	{
		if ( args.length!=1 )
			System.out.println("usage: java JSCompactor <file>");
		else
		{
			try
			{
				File f1 = new File( args[0] );
				FileInputStream fis = new FileInputStream( f1 );
				byte[] data = new byte[(int)f1.length()];
				fis.read( data );
				//String text = new String( data );
				int state = 0;
				int nSpaces = 0;
				StringBuilder sb = new StringBuilder();
				for ( int i=0;i<data.length;i++ )
				{
					char token = (char)data[i];
                    if ( token == '\r' )
                        continue;
					switch ( state )
					{
						case 0:
							if ( token == '/' )
								state = 1;
							else if ( token == '\n' )
								state = 2;
							else if ( token == ' ' )
								state = 3;
                            else 
                                append( sb, token );
							break;
						case 1:	// seen a slash
							if ( token == '*' )
								state = 4;
							//else if ( token == '/' )
							//	state = 5;
							else 
							{
								sb.append( '/' );
								if ( token == '\n' )
									state = 2;
								else if ( token == ' ' )
									state = 3;
                                else
								{
                                    append( sb, token );
									state = 0;
								}
							}
							break;
						case 2:	// seen a CR
							if ( token != '\n' )
							{
								if ( sb.length()>0&&sb.charAt(sb.length()-1)!='\n' )
                                    sb.append( "\\n" );
								if ( token == ' ' )
								{
									state = 7;
									nSpaces = 1;
								}
								else if ( token == '/' )
									state = 1;
								else
								{
									append( sb, token );
									state = 0;
								}
							}
							break;
						case 3:	// seen a space
							if ( token != ' ' )
							{
								sb.append( ' ' );
								if ( token == '\n' )
									state = 2;
								else if ( token == '/' )
									state = 1;
								else
								{
									append( sb, token );
									state = 0;
								}
							}
							break;
						case 4:	// staring a block comment
							if ( token == '*' )
								state = 6;
							break;
						case 5: // single-line comment
							if ( token == '\n' )
								state = 8;
							break;
						case 6:	// beginning of end-block-comment
							if ( token == '/' )
								state = 0;
							break;
						case 7:	// seen a space AFTER a CR
							if ( token == ' ' )
								nSpaces++;								
							else if ( token == '/' )
								state = 9;
							else if ( token == '\n' )
								state = 2;
							else
							{
								int nTabs = nSpaces/TAB_SIZE;
								for ( int j=0;j<nTabs;j++ )
									sb.append( "\\t" );
								append( sb, token );
								state = 0;
							}
							break;
						case 8:	// seen a CR at the end of a one-line comment
							if ( token == ' ' )
								state = 7;
							else if ( token == '\n' )
								state = 2;
							else if ( token == '/' )
								state = 9;
							else
							{
								append( sb, token );
								state = 0;
							}
							break;
						case 9:	// seen '/' after spaces after CR
							if ( token == '*' )
								state = 4;
							else if ( token == '/' )
								state = 5;
							else
							{
								int nTabs = nSpaces/TAB_SIZE;
								for ( int j=0;j<nTabs;j++ )
									sb.append( "\\t" );
								sb.append( '/' );
								append( sb, token );
								state = 0;
							}
							break;
					}
				}
                //System.out.println("state="+state);
                chunkify( sb.toString() );
			}
			catch ( Exception e )
			{
				e.printStackTrace( System.out );
			}
		}
	}
}