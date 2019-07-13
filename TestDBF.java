/*

     DBF JAVA CLASS - DBF READING CLASS FOR JAVA
     Copyright (C) 1996 David A Pearson
   
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the license, or 
     (at your option) any later version.
     
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
     
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

import java.io.IOException;

public class TestDBF
{
    public static void main( String argv[] ) 
    {
	DBF db = new DBF();
	
	try 
	{
	    if ( db.open( "javatest.dbf" ) )
	    {
		System.out.println( "Opened " + db.fileName() );
		System.out.println( "   lupdate() == " + db.lupdate() );
		System.out.println( "   lastrec() == " + db.lastrec() );
		System.out.println( "   recsize() == " + db.recsize() );
		System.out.println( "fieldcount() == " + db.fieldCount() );
		dumpStru( db );

		while ( !db.eof() )
		{
		    System.out.println( db.recno() + " " + 
				       db.fieldGet( "TEST_CHAR" ) + " " +
				       db.fieldGet( "TEST_DATE" ) + " " +
				       db.fieldGet( "TEST_NUM"  ) + " " +
				       db.fieldGet( "TEST_LOG" ) );
		    db.skip();
		}
		db.close();
	    }
	    else
	    {
		System.out.println( "Can't open " + db.fileName() );
	    }
	}
	catch ( IOException e )
	{
	    System.out.println( "Oops! There was a problem!" );
	}
    }
    
    static void dumpStru( DBF db )
    {
	for ( int i = 0; i < db.fcount(); i++ )
	{
	    System.out.println( db.fieldName( i ) + "\t" +
			       db.fieldType( i ) + "\t" +
			       db.fieldLen( i ) + "\t" +
			       db.fieldDec( i ) );
	}
    }
}
