package library;

//STEP 1. Import required packages
import java.io.File;
import java.util.Scanner;
//import java.util.Date;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

// import java.lang

public class LibraryManager 
{
	 // JDBC driver name and database URL
	 static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  //new driver
	 //class `com.mysql.jdbc.Driver' is deprecated
	 static final String DB_URL = "jdbc:mysql://localhost/ZhejiangUniversity";
	
	 //  Database credentials
	 static final String USER = "root";
	 static final String PASS = "MySQL123";
	 
	 public static void AddBook(PreparedStatement stmtNewBook, PreparedStatement stmtUpdateBook,
			 long ISBN, String title, String author, int year, int instock)
	 {
		int rows = 0; 
	 	try {
			stmtNewBook.setLong(1,ISBN);
			stmtNewBook.setString(2, title);
		    stmtNewBook.setString(3, author);
		    stmtNewBook.setInt(4, year);
		    stmtNewBook.setInt(5, instock);
		} catch (SQLException e) {
			System.out.println("Prepared Statement Error");
		}
	    
	    
	    try 
	    {
	    	rows = stmtNewBook.executeUpdate();
	    }catch(SQLException sqleb) {
	    	try
	    	{
	    		stmtUpdateBook.setInt(1, instock);
			    stmtUpdateBook.setLong(2,ISBN);
		    	rows = stmtUpdateBook.executeUpdate();
	    	}catch(SQLException sqleb2) {
	    		System.out.println("Cannot Update Book Records");
	    	}
	    }finally {
	    	System.out.println("Rows impacted : " + rows );
	    	rows = 0;
	    }
	 }
	 
	 public static void AddCard(PreparedStatement stmtNewCard, PreparedStatement stmtUpdateCard,
			 long ID, String name, int credit, int fining)
	 {
		int rows = 0;
		try {
	 	stmtNewCard.setLong(1, ID);
	    stmtNewCard.setString(2, name);
	    stmtNewCard.setInt(3, credit);
	    stmtNewCard.setInt(4, fining);
	 	} catch (SQLException e) {
			System.out.println("Prepared Statement Error");
		}

	    try 
	    {
	    	rows = stmtNewCard.executeUpdate();
	    }catch(SQLException sqlec) {
	    	try
	    	{
	    		stmtUpdateCard.setInt(1, credit);
    		    stmtUpdateCard.setInt(2, fining);
    		    stmtUpdateCard.setLong(3, ID);
		    	rows = stmtUpdateCard.executeUpdate();
	    	}catch(SQLException sqlec2) {
	    		System.out.println("Cannot Update Card Records");
	    	}
	    }finally {
	    	System.out.println("Rows impacted : " + rows );
	    	rows = 0;
	    }
	 }
	 
	 public static void Borrow(PreparedStatement stmtUpdateBook, PreparedStatement stmtUpdateCard,
			 PreparedStatement stmtBorrow, Calendar cal,
			 long ISBN, long ID, int year, int month, int day)
	 {
		int rows = 0;
		try {
		 	stmtUpdateBook.setInt(1, -1);
		    stmtUpdateBook.setLong(2,ISBN);
		    stmtUpdateCard.setInt(1, -1);
		    stmtUpdateCard.setInt(2, 0);
		    stmtUpdateCard.setLong(3, ID);
		}catch (SQLException e) {
			System.out.println("Prepared Statement Error");
		}
		 
		 
		try 
		{
			rows = stmtUpdateBook.executeUpdate();
			try
			{
				rows = stmtUpdateCard.executeUpdate();
			}catch(SQLException sqlebb2) {
				stmtUpdateBook.setInt(1, 1);
				rows = stmtUpdateBook.executeUpdate();
		    	System.out.println("No more credit for this card");
		}		    				
	    }catch(SQLException sqlebb) {
	    	System.out.print("No more book in stock, the nearest due date is:");
	    	// get recent duedate
	    	sql="SELECT duedate FROM borrows WHERE ISBN="+ISBN+" ORDER BY duedate LIMIT 1";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{
			       //Retrieve by column name
			       Timestamp duedate = rs.getTimestamp("duedate");
			
			       //Display values
			       System.out.println(", duedate: " + duedate);
			}
			rs.close();
		}finally {
			try 
			{
				stmtBorrow.setLong(1,ISBN);
			    stmtBorrow.setLong(2,ID);
			    cal.setTimeInMillis(System.currentTimeMillis());
			    cal.set(year, month-1, day);   
			    Timestamp currentTime = new Timestamp(cal.getTime().getTime());
			    stmtBorrow.setTimestamp(3, currentTime);
			
				rows = stmtBorrow.executeUpdate();
			}catch(SQLException sqlebb) {
		    	System.out.println("Cannot add borrow record!");
		}
		    
	    	System.out.println("Rows impacted : " + rows );
	    	rows = 0;
	    }
	 }
	 
	 public static void Return(PreparedStatement stmtUpdateBook, PreparedStatement stmtUpdateCard,
			 PreparedStatement stmtDuedate, PreparedStatement stmtReturn,
			 long ISBN, long ID)
	 {
		int rows = 0;
		int newfine = 0;
		ResultSet rs = null;
		try {
		 	stmtDuedate.setLong(1, ISBN);
			stmtDuedate.setLong(2, ID);
			stmtReturn.setLong(1, ISBN);
		    stmtReturn.setLong(2, ID);
			stmtUpdateCard.setInt(1, 1);
			stmtUpdateCard.setInt(2, newfine);
			stmtUpdateCard.setLong(3, ID);
			stmtUpdateBook.setInt(1, 1);
		    stmtUpdateBook.setLong(2, ISBN);
		} catch (SQLException e) {
			System.out.println("Prepared Statement Error");
		}  
		
		try {
			rs = stmtDuedate.executeQuery();
		}catch(SQLException sqlrs) {
			System.out.println("Cannot query given SQL");
		}
		
		try {
			if(rs.next())
			{
				try 
				{
					Timestamp duedate = rs.getTimestamp("duedate");
					Timestamp currentdate = new Timestamp(System.currentTimeMillis());
					if(duedate.before(currentdate)) 
					{
						//fining
						newfine = currentdate.compareTo(duedate);
						stmtUpdateCard.setInt(2, newfine);
					}
					
					rows = stmtReturn.executeUpdate();
					stmtUpdateCard.executeUpdate();
					stmtUpdateBook.executeUpdate();
					System.out.println("Rows impacted : " + rows );
				}catch(SQLException sqler) {
			    	System.out.println("Cannot return now!");
			}
				
			}
			else
			{
				System.out.println("No corresponding borrow info!");
			}
		} catch (SQLException e) {
			System.out.println("Query return NULL!");
		}
	 }
	 
	 
	 public static void DeleteBook(PreparedStatement stmtDeleteBook, long ISBN)
	 {
		 int rows = 0;
		 try {
			 stmtDeleteBook.setLong(1, ISBN);
			 System.out.println("Rows impacted : " + rows );
		 } catch (SQLException e) {
				System.out.println("Cannot delete book!");
		 }
	 }
	 
	 public static void DeleteCard(PreparedStatement stmtDeleteCard, long ID)
	 {
		 int rows = 0;
		 try {
			 stmtDeleteCard.setLong(1, ID);
			 System.out.println("Rows impacted : " + rows );
		 } catch (SQLException e) {
				System.out.println("Cannot delete card!");
		 }		 
	 }
	 
	 
	 public static void main(String[] args) 
	 {
		 Scanner stdIn = new Scanner(System.in);
		 Connection conn = null;
		 Statement stmt = null;
		 boolean readNext = true;
		 
		 try
		 {
		    //STEP 2: Register JDBC driver
		    Class.forName("com.mysql.cj.jdbc.Driver");
		
		    //STEP 3: Open a connection
		    System.out.println("Connecting to database...");
		    conn = DriverManager.getConnection(DB_URL,USER,PASS);
		    stmt = conn.createStatement();
		    
		    
		    Calendar cal = Calendar.getInstance();
		    cal.setTimeInMillis(System.currentTimeMillis());
		    Timestamp currentTime = new Timestamp(cal.getTime().getTime());
		    
		    String sql;
		    sql = "INSERT INTO books VALUES (?,?,?,?,?)";		    		
		    PreparedStatement  stmtNewBook = conn.prepareStatement(sql);
		    stmtNewBook.setLong(1,(long)0);
		    stmtNewBook.setString(2, "Bible");
		    stmtNewBook.setString(3, "Thy");
		    stmtNewBook.setInt(4, 1000);
		    stmtNewBook.setInt(5, 3);
		    
		    sql = "UPDATE books SET instock=instock+? WHERE ISBN=?";
		    PreparedStatement stmtUpdateBook = conn.prepareStatement(sql);
		    stmtUpdateBook.setInt(1, 0);
		    stmtUpdateBook.setLong(2,(long)0);
		    
		    sql = "INSERT INTO cards VALUES (?,?,?,?)";
		    PreparedStatement stmtNewCard = conn.prepareStatement(sql);
		    stmtNewCard.setLong(1, (long)0);
		    stmtNewCard.setString(2, "Ming");
		    stmtNewCard.setInt(3, 10);
		    stmtNewCard.setInt(4, 0);
		    
		    sql = "UPDATE cards SET credit=credit+?, fining=fining+? WHERE ID=?";
		    PreparedStatement stmtUpdateCard = conn.prepareStatement(sql);
		    stmtUpdateCard.setInt(1, 1);
		    stmtUpdateCard.setInt(2, 0);
		    stmtUpdateCard.setLong(3, (long)0);
		    
		    sql = "INSERT INTO borrows VALUES (?,?,?)";
		    PreparedStatement stmtBorrow = conn.prepareStatement(sql);
		    stmtBorrow.setLong(1,(long)0);
		    stmtBorrow.setLong(2,(long)0);
//		    date = new java.sql.Date(System.currentTimeMillis());
//		    Timestamp timestamp = new Timestamp(date.getTime());
//		    preparedStatement.setTimestamp(1, timestamp);
		    stmtBorrow.setTimestamp(3, currentTime);
		    
		    sql = "DELETE FROM books WHERE ISBN=?";
		    PreparedStatement stmtDeleteBook = conn.prepareStatement(sql);
		    stmtDeleteBook.setLong(1,(long)0);
		    
		    sql = "DELETE FROM cards WHERE ID=?";
		    PreparedStatement stmtDeleteCard = conn.prepareStatement(sql);
		    stmtDeleteCard.setLong(1,(long)0);

		    sql = "DELETE FROM borrows WHERE ISBN=? AND ID=? LIMIT 1";
		    PreparedStatement stmtReturn = conn.prepareStatement(sql);
		    stmtReturn.setLong(1,(long)0);
		    stmtReturn.setLong(2,(long)0);
		    
		    sql = "SELECT duedate FROM borrows WHERE ISBN=? AND ID=?";
		    PreparedStatement stmtDuedate = conn.prepareStatement(sql);
		    stmtReturn.setLong(1,(long)0);
		    stmtReturn.setLong(2,(long)0);
		    
		    sql = "SELECT * FROM books WHERE ?";
		    PreparedStatement stmtSelectBook = conn.prepareStatement(sql);
		    stmtSelectBook.setString(1, "1=1");
		    
		    sql = "SELECT * FROM cards WHERE ?";
		    PreparedStatement stmtSelectCard = conn.prepareStatement(sql);
		    stmtSelectCard.setString(1, "1=1");
		    
		    sql = "SELECT * FROM borrows WHERE ?";
		    PreparedStatement stmtSelectBorrow = conn.prepareStatement(sql);
		    stmtSelectBorrow.setString(1, "1=1");
		    
		    sql = "SELECT * FROM ?";
		    PreparedStatement stmtSelect = conn.prepareStatement(sql);
		    stmtSelect.setString(1, "borrows");
		    
		    
		    
		
		    while(readNext)
		    {
		    	System.out.println("Guide: input number to select actions");
		    	System.out.println("1: Create Schema and Table;");
		    	System.out.println("2: New/Update Book; 3: New/Update Card");
		    	System.out.println("4: Borrow; 5: Return");
		    	System.out.println("6: Select from books; 7: Select from cards; 8: Select from borrows");
		    	System.out.println("9: Batch Operations; Others: Exit");
		    	int mode=stdIn.nextInt();
		    	
		    	switch(mode) {
		    		case 1://create
		    		{
		    		    sql = "CREATE TABLE IF NOT EXISTS books ("
		    		    		+ "ISBN BIGINT PRIMARY KEY NOT NULL, "
		    		    		+ "title VARCHAR(50) NOT NULL, " 
		    		    		+ "author VARCHAR(50),"
		    		    		+ "year INT,"
		    		    		+ "instock INT, "
		    		    		+ "CONSTRAINT nonegativestock CHECK (instock>=0) )";
		    		    stmt.executeUpdate(sql);
		    		    
		    		    sql = "CREATE TABLE IF NOT EXISTS cards ("
		    		    		+ "ID INT PRIMARY KEY NOT NULL,"
		    		    		+ "name VARCHAR(50) NOT NULL,"
		    		    		+ "credit INT,"
		    		    		+ "fining INT,"
		    		    		+ "CONSTRAINT nonegativecredit CHECK (credit>=0),"
		    		    		+ "CONSTRAINT nonegativefining CHECK (fining>=0) )";
		    		    stmt.executeUpdate(sql);
		    		    
		    		    sql = "CREATE TABLE IF NOT EXISTS borrows ("
		    		    		+ "ISBN BIGINT NOT NULL,"
		    		    		+ "ID INT NOT NULL,"
		    		    		+ "duedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
		    		    		+ "FOREIGN KEY (ISBN) REFERENCES books (ISBN)"
		    		    		+ "    ON UPDATE RESTRICT ON DELETE CASCADE,"
		    		    		+ "FOREIGN KEY (ID) REFERENCES cards (ID)"
		    		    		+ "    ON UPDATE RESTRICT ON DELETE CASCADE )";
		    		    stmt.executeUpdate(sql);
		    		    		    			
		    			break;
		    		}	
		    		case 2://new book
		    		{
					    System.out.println("New Book into stock...");
					    System.out.println("ISBN: ");
					    long ISBN = stdIn.nextLong();
					    System.out.println("Title: ");
					    String title = stdIn.next();
					    System.out.println("Author: ");
					    String author = stdIn.next();
					    System.out.println("Year: ");
					    int year = stdIn.nextInt();
					    System.out.println("Instock: ");
					    int instock = stdIn.nextInt();
					    
					    
					    AddBook(stmtNewBook, stmtUpdateBook, ISBN, title, author, year, instock);
					    break;
		    		}
		    		case 3://new card
		    		{
		    			System.out.println("New Card into stock...");
		    			System.out.println("ID: ");
		    			long ID = stdIn.nextLong();
		    			System.out.println("name: ");
		    			String name = stdIn.next();
		    			System.out.println("credit: ");
		    			int credit = stdIn.nextInt();
		    			System.out.println("fining: ");
		    			int fining = stdIn.nextInt();
		    			
		    			AddCard(stmtNewCard, stmtUpdateCard, ID, name, credit, fining);
		    		    
		    			break;
		    		}
		    		case 4://borrow book
		    		{
		    			System.out.println("Try to borrow book...");
		    			System.out.println("ISBN: ");
					    long ISBN = stdIn.nextLong();
		    			System.out.println("ID: ");
		    			long ID = stdIn.nextLong();
		    			System.out.println("Due Year: ");
		    			int year = stdIn.nextInt();
		    			System.out.println("Due Month: ");
		    			int month = stdIn.nextInt();
		    			System.out.println("Due Day: ");
		    			int day = stdIn.nextInt();
		    			
		    			Borrow(stmtUpdateBook, stmtUpdateCard, stmtBorrow, cal,
		    					  ISBN, ID, year, month, day);
		    			

		    			break;
		    		}
		    		case 5://return book
		    		{
		    			System.out.println("Try to return book...");
		    			System.out.println("ISBN: ");
					    long ISBN = stdIn.nextLong();
		    			System.out.println("ID: ");
		    			long ID = stdIn.nextLong();
		    			
		    			
		    			Return(stmtUpdateBook, stmtUpdateCard, stmtDuedate, stmtReturn,
		    					  ISBN, ID);

		    			break;
		    		}
		    		case 6://select books with given where
		    		{
		    			System.out.println("Searching in books...");
		    			System.out.println("Enter your requirement in SQL grammar");
		    			String req = stdIn.next();
		    			
		    			sql="SELECT * FROM books WHERE "+req;
		    			ResultSet rs = stmt.executeQuery(sql);
		    			while(rs.next())
		    			{
						       //Retrieve by column name
						       int ISBN  = rs.getInt("ISBN");
						       String title = rs.getString("title");
						       String author = rs.getString("author");
						       int year = rs.getInt("year");
						       int instock = rs.getInt("instock");
						
						       //Display values
						       System.out.print("ISBN: " + ISBN);
						       System.out.print(", title: " + title);
						       System.out.print(", author: " + author);
						       System.out.print(", year: " + year);
						       System.out.println(", instock: " + instock);
		    			}
						rs.close();
						break;
		    		}
		    		case 7://select cards with given where
		    		{
		    			System.out.println("Searching in cards...");
		    			System.out.println("Enter your requirement in SQL grammar");
		    			String req = stdIn.next();


		    			sql="SELECT * FROM cards WHERE "+req;
		    			ResultSet rs = stmt.executeQuery(sql);
		    			while(rs.next())
		    			{
						       //Retrieve by column name
						       int ID  = rs.getInt("ID");
						       String name = rs.getString("name");
						       int credit = rs.getInt("credit");
						       int fining = rs.getInt("fining");
						
						       //Display values
						       System.out.print("ID: " + ID);
						       System.out.print(", name: " + name);
						       System.out.print(", credit: " + credit);
						       System.out.println(", fining: " + fining);
		    			}
						rs.close();
						break;
		    		}
		    		case 8://select borrows with given where
		    		{
		    			System.out.println("Searching in borrows...");
		    			System.out.println("Enter your requirement in SQL grammar");
		    			String req = stdIn.next();
		    			
		    			sql="SELECT * FROM borrows WHERE "+req;
		    			ResultSet rs = stmt.executeQuery(sql);
		    			while(rs.next())
		    			{
						       //Retrieve by column name
						       int ISBN  = rs.getInt("ISBN");
						       int ID = rs.getInt("ID");
						       Timestamp duedate = rs.getTimestamp("duedate");
						
						       //Display values
						       System.out.print("ISBN: " + ISBN);
						       System.out.print(", ID: " + ID);
						       System.out.println(", duedate: " + duedate);
		    			}
						rs.close();
		    			break;
		    		}
		    		case 9://batch input
		    		{
		    			System.out.println("1: Books Info from File; 2. Card Info from File; 3. Borrow Info from File");
		    			int batchwhat = stdIn.nextInt();
		    			System.out.println("Input filename:");
		    			String filename = stdIn.next();
		    			File file = new File(filename);
		    			if(file.canRead())
		    			{
			    			Scanner fi = new Scanner(file);
			    			switch(batchwhat)
			    			{
				    			case 1:
				    			{
				    				while (fi.hasNextLine())// C:\\Orga\\DB5\\bookinput
				    				{
				    					long ISBN = fi.nextLong();
									    String title = fi.next();
									    String author = fi.next();
									    int year = fi.nextInt();
									    int instock = fi.nextInt();
									    								    
									    AddBook(stmtNewBook, stmtUpdateBook, ISBN, title, author, year, instock);
				    				}
				    				break;
				    			}
				    			case 2:
				    			{
				    				while (fi.hasNextLine())// C:\\Orga\\DB5\\cardinput
				    				{
					    				long ID = fi.nextLong();
						    			String name = fi.next();
						    			int credit = fi.nextInt();
						    			int fining = fi.nextInt();
						    			AddCard(stmtNewCard, stmtUpdateCard, ID, name, credit, fining);
				    				}
				    				break;
				    			}
				    			case 3:// C:\\Orga\\DB5\\borrowinput
				    			{
				    				while (fi.hasNextLine())
				    				{
					    				long ISBN = fi.nextLong();
						    			long ID = fi.nextLong();
						    			int year = fi.nextInt();
						    			int month = fi.nextInt();
						    			int day = fi.nextInt();
						    			
						    			Borrow(stmtUpdateBook, stmtUpdateCard, stmtBorrow, cal, 
						    					ISBN, ID, year, month, day);
				    				}
				    				break;
				    			}
				    			default:
				    			{			    				
				    				break;
				    			}
				    				
			    			}
			    			fi.close();
		    			}
		    			break;
		    			
		    		}
		    		case 10://delete book
		    		{
		    			System.out.println("Deleting book...");
		    			System.out.println("ISBN:");
		    			long ISBN = stdIn.nextLong();
		    			
		    			DeleteBook(stmtDeleteBook, ISBN);
		    			break;
		    		}
		    		case 11://delete card
		    		{
		    			System.out.println("Deleting card...");
		    			System.out.println("ID:");
		    			long ID = stdIn.nextLong();
		    			
		    			DeleteBook(stmtDeleteCard, ID);
		    			break;
		    		}
		    		default://exit
		    		{
		    			readNext=false;
		    			break;
		    		}
		    	}
		    	
		    	
		    	
		    }

			//STEP 6: Clean-up environment
		    stmt.close();
		    conn.close();

		 }catch(SQLException se){
		    //Handle errors for JDBC
		    se.printStackTrace();
		 }catch(Exception e){
		    //Handle errors for Class.forName
		    e.printStackTrace();
		 }finally{
		    //finally block used to close resources
		    try{
		       if(stmt!=null)
		          stmt.close();
		    }catch(SQLException se2){
		    }// nothing we can do
		    try{
		       if(conn!=null)
		          conn.close();
		    }catch(SQLException se){
		       se.printStackTrace();
		    }//end finally try
		 }//end try
		 System.out.println("Goodbye!");
	 }//end main
}//end FirstExample
