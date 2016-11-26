package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * {@link CertainBookStore} implements the {@link BookStore} and
 * {@link StockManager} functionalities.
 * 
 * @see BookStore
 * @see StockManager
 */
public class CertainBookStore implements BookStore, StockManager {

	/** The mapping of books from ISBN to {@link BookStoreBook}. */
	private Map<Integer, BookStoreBook> bookMap = null;
	
	/** The mapping of books with ISBN and rating */
	//private Map<Integer, BookRating> bookRatings = null;

	/**
	 * Instantiates a new {@link CertainBookStore}.
	 */
	public CertainBookStore() {

		// Constructors are not synchronized
		this.bookMap = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addBooks(java.util.Set)
	 */
	public synchronized void addBooks(Set<StockBook> bookSet) throws BookStoreException {
		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check if all are there
		for (StockBook book : bookSet) {
			int isbn = book.getISBN();
			String bookTitle = book.getTitle();
			String bookAuthor = book.getAuthor();
			int noCopies = book.getNumCopies();
			float bookPrice = book.getPrice();
			

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isEmpty(bookTitle)) {
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isEmpty(bookAuthor)) {
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isInvalidNoCopies(noCopies)) {
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (bookPrice < 0.0) {
				throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
			}

			if (bookMap.containsKey(isbn)) {
				//System.out.println(BookStoreConstants.ISBN + isbn + BookStoreConstants.DUPLICATED);
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.DUPLICATED);
			}
		}

		for (StockBook book : bookSet) {
			int isbn = book.getISBN();
			bookMap.put(isbn, new BookStoreBook(book));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addCopies(java.util.Set)
	 */
	public synchronized void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		int isbn;
		int numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
				throw new BookStoreException(BookStoreConstants.NUM_COPIES + numCopies + BookStoreConstants.INVALID);
			}
		}

		BookStoreBook book;

		// Update the number of copies
		for (BookCopy bookCopy : bookCopiesSet) {
			isbn = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			book = bookMap.get(isbn);
			book.addCopies(numCopies);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooks()
	 */
	public synchronized List<StockBook> getBooks() {
		List<StockBook> listBooks = new ArrayList<>();
		Collection<BookStoreBook> bookMapValues = bookMap.values();

		for (BookStoreBook book : bookMapValues) {
			listBooks.add(book.immutableStockBook());
		}

		return listBooks;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#updateEditorPicks(java.util
	 * .Set)
	 */
	public synchronized void updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException {

		// Check that all ISBNs that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbnValue;

		for (BookEditorPick editorPickArg : editorPicks) {
			isbnValue = editorPickArg.getISBN();

			if (BookStoreUtility.isInvalidISBN(isbnValue)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbnValue)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (BookEditorPick editorPickArg : editorPicks) {
			bookMap.get(editorPickArg.getISBN()).setEditorPick(editorPickArg.isEditorPick());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public synchronized void buyBooks(Set<BookCopy> bookCopiesToBuy) throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int isbn;
		BookStoreBook book;
		Boolean saleMiss = false;

		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			isbn = bookCopyToBuy.getISBN();

			if (bookCopyToBuy.getNumCopies() < 0) {
				throw new BookStoreException(
						BookStoreConstants.NUM_COPIES + bookCopyToBuy.getNumCopies() + BookStoreConstants.INVALID);
			}

			if (BookStoreUtility.isInvalidISBN(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
			}

			book = bookMap.get(isbn);

			if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {

				// If we cannot sell the copies of the book, it is a miss.
				book.addSaleMiss();
				saleMiss = true;
			}
		}

		// We throw exception now since we want to see how many books in the
		// order incurred misses which is used by books in demand
		if (saleMiss) {
			throw new BookStoreException(BookStoreConstants.BOOK + BookStoreConstants.NOT_AVAILABLE);
		}

		// Then make the purchase.
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			book = bookMap.get(bookCopyToBuy.getISBN());
			book.buyCopies(bookCopyToBuy.getNumCopies());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#getBooksByISBN(java.util.
	 * Set)
	 */
	public synchronized List<StockBook> getBooksByISBN(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<StockBook> listBooks = new ArrayList<>();

		for (Integer isbn : isbnSet) {
			listBooks.add(bookMap.get(isbn).immutableStockBook());
		}

		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getBooks(java.util.Set)
	 */
	public synchronized List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we rate are there to start with.
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<Book> listBooks = new ArrayList<>();

		for (Integer isbn : isbnSet) {
			listBooks.add(bookMap.get(isbn).immutableBook());
		}

		return listBooks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getEditorPicks(int)
	 */
	public synchronized List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
		}

		List<BookStoreBook> listAllEditorPicks = new ArrayList<>();
		List<Book> listEditorPicks = new ArrayList<>();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
		BookStoreBook book;

		// Get all books that are editor picks.
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = it.next();
			book = pair.getValue();

			if (book.isEditorPick()) {
				listAllEditorPicks.add(book);
			}
		}

		// Find numBooks random indices of books that will be picked.
		Random rand = new Random();
		Set<Integer> tobePicked = new HashSet<>();
		int rangePicks = listAllEditorPicks.size();

		if (rangePicks <= numBooks) {

			// We need to add all books.
			for (int i = 0; i < listAllEditorPicks.size(); i++) {
				tobePicked.add(i);
			}
		} else {

			// We need to pick randomly the books that need to be returned.
			int randNum;

			while (tobePicked.size() < numBooks) {
				randNum = rand.nextInt(rangePicks);
				tobePicked.add(randNum);
			}
		}

		// Get the numBooks random books.
		for (Integer index : tobePicked) {
			book = listAllEditorPicks.get(index);
			listEditorPicks.add(book.immutableBook());
		}

		return listEditorPicks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getTopRatedBooks(int)
	 */
	@Override
	public synchronized List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		//throw new BookStoreException();
		
		// Get all books in a list
		List<StockBook> books = this.getBooks();
		
		// If numBooks is larger than number of books in the collection or 
		// is a negative number, return an exception
		if (numBooks > books.size() || numBooks < 0){
			throw new BookStoreException(BookStoreConstants.BOOK_NUM_PARAM + BookStoreConstants.INVALID);
		}
		
		// Sort books according to their average rating
		Collections.sort(books, new Comparator<StockBook>() {
		       public int compare(StockBook o1, StockBook o2) {
		    	   float rating1 = o1.getAverageRating();
		    	   float rating2 = o2.getAverageRating();
		    	   
		    	   if (rating1 < rating2) return 1;
			       if (rating1 > rating2) return -1;
			       return 0;
		       }
		   });
		
		// Create new list for return value
		List<Book> newList = new ArrayList<>();
		
		// from the highest rated books, for each, add book to the result list
		// as immutable book to prevent changes.
		for (int i = 0; i < numBooks; i++) {
			int bookISBN = books.get(i).getISBN();
			BookStoreBook book = this.bookMap.get(bookISBN);
			newList.add(book.immutableStockBook());
		}
			
		return newList;	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooksInDemand()
	 */
	@Override
	public synchronized List<StockBook> getBooksInDemand() throws BookStoreException {
		//throw new BookStoreException();
		
		// Get all books in a list
		List<StockBook> books = this.getBooks();
				
		// Create new list for return value
		List<StockBook> newList = new ArrayList<>();
		
		// For each book, if number of missed sales are larger than 0,
		// then add to return list
		for (StockBook book : books) {
			if (book.getNumSaleMisses() > 0){
				int bookISBN = book.getISBN();
				BookStoreBook br = this.bookMap.get(bookISBN);
				newList.add(br.immutableStockBook());
			}
		}
		
		return newList;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#rateBooks(java.util.Set)
	 */
	@Override
	public synchronized void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		//throw new BookStoreException();

		// test if books are in the books list and rating is valid
		for (BookRating br : bookRating) {
			int isbn = br.getISBN();
			int rating = br.getRating();
			
			if (!this.bookMap.containsKey(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
			}
			
			if (rating < 0 || rating > 5) {
				throw new BookStoreException(BookStoreConstants.RATING + rating + BookStoreConstants.INVALID);
			}
		}
		
		
		// update the books and their ratings in the book map
		for (BookRating br : bookRating) {
			// get book from book map
			BookStoreBook book = this.bookMap.get(br.getISBN());
			
			// Update the given books rating
			book.addRating(br.getRating());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#removeAllBooks()
	 */
	public synchronized void removeAllBooks() throws BookStoreException {
		bookMap.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#removeBooks(java.util.Set)
	 */
	public synchronized void removeBooks(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
			}

			if (!bookMap.containsKey(ISBN)) {
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (int isbn : isbnSet) {
			bookMap.remove(isbn);
		}
	}
}
