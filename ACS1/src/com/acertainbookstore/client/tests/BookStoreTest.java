package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link BookStoreTest} tests the {@link BookStore} interface.
 * 
 * @see BookStore MATHIAS IS WAIFU!!!!
 */
public class BookStoreTest {

	/** The Constant TEST_ISBN. */
	private static final int TEST_ISBN = 3044560;
	
	/** List of isbn numbers for quick reference*/
	private static final int[] ISBN_ARRAY = {
			6843180, 4681614, 1614846, 7541351, 6846413
	};
	
	/** The Constant NUM_COPIES. */
	private static final int NUM_COPIES = 5;

	/** The local test. */
	private static boolean localTest = true;

	/** The store manager. */
	private static StockManager storeManager;

	/** The client. */
	private static BookStore client;

	/**
	 * Sets the up before class.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;
			//localTest = false;
			//System.out.println(localTest);

			if (localTest) {
				CertainBookStore store = new CertainBookStore();
				storeManager = store;
				client = store;
			} else {
				storeManager = new StockManagerHTTPProxy("http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}

			storeManager.removeAllBooks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to add some books.
	 *
	 * @param isbn
	 *            the isbn
	 * @param copies
	 *            the copies
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public void addBooks(int isbn, int copies) throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book = new ImmutableStockBook(isbn, "Test of Thrones", "George RR Testin'", (float) 10, copies, 0, 0,
				0, false);
		booksToAdd.add(book);
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Helper method to get the default book used by initializeBooks.
	 *
	 * @return the default book
	 */
	public StockBook getDefaultBook() {
		return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
				false);
	}
	
	// Create a list of books to be added to the StockManager
	public Set<StockBook> defineBooks(){
		Set<StockBook> books = new HashSet<StockBook>();
		
		// Add random books 
		books.add(new ImmutableStockBook(ISBN_ARRAY[0], "ACS Compendium", "Marcos", (float) 1, NUM_COPIES, 0, 0, 0, false));
		books.add(new ImmutableStockBook(ISBN_ARRAY[1], "Learn a great haskell", "Ken", (float) 15, NUM_COPIES, 0, 0, 0, false));
		books.add(new ImmutableStockBook(ISBN_ARRAY[2], "Algorithms trivial tutorial", "Mikkel", (float) 12, NUM_COPIES, 0, 0, 0, false));
		books.add(new ImmutableStockBook(ISBN_ARRAY[3], "Dank Memes", "The Internet", (float) 8, NUM_COPIES, 0, 0, 0, true));
		books.add(new ImmutableStockBook(ISBN_ARRAY[4], "Machine Learning", "SelIgel", (float) 75, NUM_COPIES, 0, 0, 0, false));
		
		// return the set
		return books;
	}

	/**
	 * Method to add a book, executed before every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Before
	public void initializeBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(getDefaultBook());
		booksToAdd.addAll(defineBooks()); // Add self defined books
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Method to clean up the book store, execute after every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@After
	public void cleanupBooks() throws BookStoreException {
		storeManager.removeAllBooks();
	}

	/**
	 * Tests basic buyBook() functionality.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyAllCopiesDefaultBook() throws BookStoreException {
		// Set of books to buy
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES));

		// Try to buy books
		client.buyBooks(booksToBuy);

		List<StockBook> listBooks = storeManager.getBooks();
		assertTrue(listBooks.size() == 6); // There are 6 books not 1 because we add another 5
		StockBook bookInList = listBooks.get(listBooks.size()-1); // DEfault book is on the last place in the list
		StockBook addedBook = getDefaultBook();

		assertTrue(bookInList.getISBN() == addedBook.getISBN() && bookInList.getTitle().equals(addedBook.getTitle())
				&& bookInList.getAuthor().equals(addedBook.getAuthor()) && bookInList.getPrice() == addedBook.getPrice()
				&& bookInList.getNumSaleMisses() == addedBook.getNumSaleMisses()
				&& bookInList.getAverageRating() == addedBook.getAverageRating()
				&& bookInList.getNumTimesRated() == addedBook.getNumTimesRated()
				&& bookInList.getTotalRating() == addedBook.getTotalRating()
				&& bookInList.isEditorPick() == addedBook.isEditorPick());
	}

	/**
	 * Tests that books with invalid ISBNs cannot be bought.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyInvalidISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with invalid ISBN.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(-1, 1)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that books can only be bought if they are in the book store.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNonExistingISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with ISBN which does not exist.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(100000, 10)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy more books than there are copies.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyTooManyBooks() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy more copies than there are in store.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES + 1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy a negative number of books.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNegativeNumberOfBookCopies() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a negative number of copies.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that all books can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetBooks() throws BookStoreException {
		Set<StockBook> booksAdded = new HashSet<StockBook>();
		booksAdded.add(getDefaultBook());
		booksAdded.addAll(defineBooks()); // remember to add our books to the compare list

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		booksAdded.addAll(booksToAdd);

		storeManager.addBooks(booksToAdd);
		
		// Get books in store.
		List<StockBook> listBooks = storeManager.getBooks();

		// Make sure the lists equal each other.
		assertTrue(listBooks.containsAll(booksAdded) && listBooks.size() == booksAdded.size());
	}

	/**
	 * Tests that a list of books with a certain feature can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetCertainBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		storeManager.addBooks(booksToAdd);

		// Get a list of ISBNs to retrieved.
		Set<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN + 1);
		isbnList.add(TEST_ISBN + 2);

		// Get books with that ISBN.
		List<Book> books = client.getBooks(isbnList);

		// Make sure the lists equal each other
		assertTrue(books.containsAll(booksToAdd) && books.size() == booksToAdd.size());
	}

	/**
	 * Tests that books cannot be retrieved if ISBN is invalid.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetInvalidIsbn() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Make an invalid ISBN.
		HashSet<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN); // valid
		isbnList.add(-1); // invalid

		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.getBooks(isbnList);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}
	
	
	// Rate a book correctly and check if it is a part of rated books
	@Test
	public void testRateBookCorrectly() throws BookStoreException {
			// *** Give a book a rating
		// Get all books in the store
		List<StockBook> booksInStorePreTest = storeManager.getBooks();
		
		// Get a book
		StockBook aBook = booksInStorePreTest.get(0);
		
		// Rate A book
		BookRating br = new BookRating(aBook.getISBN(), 1);
		Set<BookRating> setBr = new HashSet<BookRating>();
		setBr.add(br);
		
		// Set the rating
		client.rateBooks(setBr);
		
			// *** Get the rated book
		// Get all books in the store
		List<Book> booksInStoreDuringTest = client.getTopRatedBooks(booksInStorePreTest.size());
		
		// Assert result
		assertTrue(booksInStoreDuringTest.contains(aBook));
	}
	
	
	// Rate a book incorrectly, rating is out of scale
	@Test(expected=BookStoreException.class)
	public void testRateBookWrong() throws BookStoreException {
			// *** Give a book a rating
		// Get all books in the store
		List<StockBook> booksInStorePreTest = storeManager.getBooks();
		
		// Get a book
		StockBook aBook = booksInStorePreTest.get(1);
		
		// Rate A book
		BookRating br = new BookRating(aBook.getISBN(), 10000);
		Set<BookRating> setBr = new HashSet<BookRating>();
		setBr.add(br);
		
		// Set the rating
		client.rateBooks(setBr);
		
			// *** Get the rated book
		// Get all books in the store
		//List<Book> booksInStoreDuringTest = client.getTopRatedBooks(booksInStorePreTest.size());
		
		// Assert result
		//assertTrue(!(booksInStoreDuringTest.contains(aBook)));
	}
	
	
	// Rate a book that does not exist
	@Test(expected=BookStoreException.class)
	public void testRateBookNotExist() throws BookStoreException {
			// *** Give a book a rating
		// Get all books in the store
		//List<StockBook> booksInStorePreTest = storeManager.getBooks();
		
		// Rate A book that does not exist (isbn 42)
		BookRating br = new BookRating(42, 4);
		Set<BookRating> setBr = new HashSet<BookRating>();
		setBr.add(br);
		
		// Set the rating
		client.rateBooks(setBr);
		
	}
	
	// Rate a book to 1 and then change the rating to it becomes the most popular book
	@Test
	public void testRateBookRated() throws BookStoreException {
			// *** Give a book a rating
		// Get all books in the store
		List<StockBook> booksInStorePreTest = storeManager.getBooks();
		
		// Get a book
		StockBook aBook = booksInStorePreTest.get(2);
		StockBook bBook = booksInStorePreTest.get(3);
		
		// Rate A book
		BookRating br  = new BookRating(aBook.getISBN(), 1);
		BookRating br2 = new BookRating(bBook.getISBN(), 2);
		Set<BookRating> setBr = new HashSet<BookRating>();
		setBr.add(br);
		setBr.add(br2);
		
		// Set the rating
		client.rateBooks(setBr);
		
		// Get the top one book at this point
		List<Book> topOneBookFirstRating = client.getTopRatedBooks(1);
		
		// Rate A book
	    br = new BookRating(aBook.getISBN(), 5);
		setBr = new HashSet<BookRating>();
		setBr.add(br);
		
		// Set the rating
		client.rateBooks(setBr);
		
			// *** Get the rated book
		// Get all books in the store
		List<Book> topSecondBookFirstRating = client.getTopRatedBooks(1);
		
		// Assert result
		assertTrue(topOneBookFirstRating.contains(bBook) && 
				   topSecondBookFirstRating.contains(aBook));
	}
	
	// Try and get more rated books than there are books in the bookstore
	@Test(expected=BookStoreException.class)
	public void testGetTopRatedBooksTooMany() throws BookStoreException {
		// Get all books in the book store
		List<StockBook> booksInStorePreTest = storeManager.getBooks();
		
		// Get top rated books more than there exist
		client.getTopRatedBooks(booksInStorePreTest.size() + 1);
		
	}
	
	// Try and get a negative number of top rated books
	@Test(expected=BookStoreException.class)
	public void testGetTopRatedBooksNegativeNumber() throws BookStoreException {
		client.getTopRatedBooks(-1);
		
	}

	/**
	 * Tear down after class.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();

		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
