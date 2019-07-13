/*

     DBF JAVA CLASS
     Copyright (C) 1996  David A Pearson
   
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 1, or (at your option)
     any later version.
     
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
     
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Hashtable;

class DBF
{
    protected String           strFileName;
    protected File             fleInfo;
    protected RandomAccessFile fleHandle;
    protected boolean          boolIsOpen;
    protected Date             dUpdated;
    protected long             lLastRec;
    protected int              iDataOffset;
    protected int              iRecSize;
    protected int              iFieldCount;
    protected String           strFldName[];
    protected String           strFldType[];
    protected int              iFldLen[];
    protected int              iFldDec[];
    protected long             lRecNo;
    protected Boolean          boolDeleted;
    protected Hashtable        recContents;

    DBF()
    {
	boolIsOpen  = false;
	strFileName = "";
	lRecNo      = 0;
	recContents = new Hashtable();
    }
    
    boolean open( String s ) throws IOException
    {
	close();   // Just in case.
	
	boolIsOpen  = false;
	strFileName = s;
	
	fleInfo = new File( s );
	
	if ( fleInfo.exists() && fleInfo.isFile() ) 
	{
	    try 
	    {
		fleHandle  = new RandomAccessFile( fleInfo, "rw" );
		boolIsOpen = true;
		if ( readHeader() )
		{
		}
		else 
		{
		    close();
		}
	    }
	    catch( IOException e )
	    {
		boolIsOpen = false;
	    }
	}
	
	return( boolIsOpen );
    }
    
    void close() throws IOException
    {
	if ( boolIsOpen )
	{
	    fleHandle.close();
	    boolIsOpen  = false;
	    strFileName = "";
	}
    }
    
    String fileName()
    {
	return( strFileName );
    }

    protected boolean readHeader() throws IOException
    {
	boolean Ok = false;
	int iID    = fleHandle.read();

	if ( isDbfId( iID ) )
	{
	    int iYear   = fleHandle.read();
	    int iMonth  = fleHandle.read();
	    int iDay    = fleHandle.read();
	    dUpdated    = new Date( iYear, iMonth - 1, iDay );
	    lLastRec    = getLong();
	    iDataOffset = getShort();
	    iRecSize    = getShort();
	    iFieldCount = ( ( iDataOffset - 1 ) / 32 ) - 1;

	    fleHandle.skipBytes( 20 );
	    
	    loadFieldInfo();

	    lRecNo = 1;
	    readRecord();

	    Ok = true;
	}
	else
	{
	    // Handle this correctly.
	    System.out.println( "Not a dBase file!" );
	}

	return( Ok );
    }

    protected void loadFieldInfo() throws IOException
    {
	byte nameBuff[] = new byte[ 10 ];

	strFldName = new String[ iFieldCount ];
	strFldType = new String[ iFieldCount ];
        iFldLen    = new int[ iFieldCount ];
        iFldDec    = new int[ iFieldCount ];	

	for ( int i = 0; i < iFieldCount; i++ )
	{
	    strFldName[ i ] = new String( getNullString( 11 ) );
	    strFldType[ i ] = new String( getNullString(  1 ) );
	    
	    fleHandle.skipBytes( 4 );

	    if ( strFldType[ i ].equals( "C" ) )
	    {
		iFldLen[ i ] = getShort();
		iFldDec[ i ] = 0;
	    }
	    else
	    {
		iFldLen[ i ] = (int) fleHandle.read();
		iFldDec[ i ] = (int) fleHandle.read();
	    }

	    fleHandle.skipBytes( 14 );
	}
    }
    
    Date lupdate()
    {
	return( dUpdated );
    }

    long lastrec()
    {
	return( lLastRec );
    }

    int recsize()
    {
	return( iRecSize );
    }

    int fcount()
    {
	return( iFieldCount );
    }

    int fieldCount()
    {
	return( iFieldCount );
    }

    String fieldName( int iField )
    {
	return( strFldName[ iField ] );
    }

    String fieldType( int iField )
    {
	return( strFldType[ iField ] );
    }

    int fieldLen( int iField )
    {
	return( iFldLen[ iField ] );
    }

    int fieldDec( int iField )
    {
	return( iFldDec[ iField ] );
    }

    int headerSize()
    {
	return( iDataOffset );
    }

    long recno()
    {
	return( lRecNo );
    }

    void goTop() throws IOException
    {
	lRecNo = 1;
	readRecord();
    }

    void goBottom() throws IOException
    {
	lRecNo = lastrec();
	readRecord();
    }

    void skip() throws IOException
    {
	skip( 1 );
    }

    void skip( long l ) throws IOException
    {
	lRecNo += l;
	if ( lRecNo > lastrec() )
	{
	    lRecNo = lastrec() + 1;
	}
	readRecord();
    }

    boolean bof()
    {
	return( false );
    }

    boolean eof()
    {
	return( recno() > lastrec() );
    }
    
    Object fieldGet( String strName )
    {
	return( recContents.get( strName ) );
    }

    Object fieldGet( int iField )
    {
	return( fieldGet( strFldName[ iField ] ) );
    }

    protected void readRecord() throws IOException
    {
	if ( !eof() )
	{
	    fleHandle.seek( headerSize() + ( recsize() * ( recno() - 1 ) ) );
	}
	    
	byte buffer[] = new byte[ recsize() ];

	if ( !eof() )
	{
	    fleHandle.read( buffer, 0, recsize() );
	}
	    
	int iOffset = 1;
	    
	for ( int i = 0; i < fcount(); i++ )
	{
	    if ( eof() )
	    {
		recContents.put( strFldName[ i ], "" );
	    }
	    else
	    {
		loadField( buffer, i, iOffset );
		iOffset += iFldLen[ i ];
	    }
	}
    }

    protected void loadField( byte buffer[], int iField, int iOffset )
    {
	if ( strFldType[ iField ].equals( "C" ) )
	{
	    recContents.put( strFldName[ iField ],
			    fieldAsString( buffer, iOffset, iField ) );
	}
	else if ( strFldType[ iField ].equals( "N" ) )
	{
	    recContents.put( strFldName[ iField ],
			    new Float( fieldAsString( buffer, iOffset,
						     iField ) ) );
	}
	else if ( strFldType[ iField ].equals( "D" ) )
	{
	    recContents.put( strFldName[ iField ],
			    fieldAsDate( buffer, iOffset, iField ) );
	}
	else if ( strFldType[ iField ].equals( "L" ) )
	{
	    recContents.put( strFldName[ iField ], 
			    new Boolean( buffer[ iOffset ] == 'T' ) );
	}
	else if ( strFldType[ iField ].equals( "M" ) )
	{
	    recContents.put( strFldName[ iField ], "<Memo>" );
	}
	else
	{
	    // Oops! Should be an exception.
	}
    }

    protected String fieldAsString( byte buffer[], int iOffset, int iField )
    {
	StringBuffer b = new StringBuffer();

	for ( int i = 0; i < iFldLen[ iField ]; i++ )
	{
	    b.append( (char) buffer[ iOffset + i ] );
	}

	return( new String( b ) );
    }

    protected Date fieldAsDate( byte buffer[], int iOffset, int iField )
    {
	StringBuffer strYear  = new StringBuffer();
	StringBuffer strMonth = new StringBuffer();
	StringBuffer strDay   = new StringBuffer();
	int i;

	for ( i = 0; i < 4; i++ )
	{
	    strYear.append( (char) buffer[ iOffset + i ] );
	}
	for ( i = i; i < 6; i++ )
	{
	    strMonth.append( (char) buffer[ iOffset + i ] );
	}
	for ( i = i; i < 8; i++ )
	{
	    strDay.append( (char) buffer[ iOffset + i ] );
	}

	return( new Date( Integer.parseInt( new String( strYear ) ) - 1900,
			 Integer.parseInt( new String( strMonth ) ) - 1,
			 Integer.parseInt( new String( strDay ) ) ) );
    }

    protected boolean isDbfId( int iID )
    {
	return( iID == 0x3 || iID == 0x83 );
    }

    protected int getShort() throws IOException
    {
	int i1 = fleHandle.read();
	int i2 = fleHandle.read();

	return( (int) ( i2 << 8 ) + i1 );
    }

    protected long getLong() throws IOException
    {
	int i1 = getShort();
	int i2 = getShort();

	return( (long) ( i2 << 16 ) + i1 );
    }

    protected StringBuffer getNullString( int iLen ) throws IOException
    {
	byte b[]         = new byte[ iLen ];
	StringBuffer str = new StringBuffer();

	fleHandle.read( b, 0, iLen );

	for ( int i = 0; i < iLen && b[ i ] != 0 ; i++ )
	{
	    str.append( (char) b[ i ] );
	}

	return( str );
    }
}
