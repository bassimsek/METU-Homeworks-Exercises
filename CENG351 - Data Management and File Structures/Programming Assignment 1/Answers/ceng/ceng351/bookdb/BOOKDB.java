package ceng.ceng351.bookdb;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BOOKDB {

    private static String user = "example_user";
    private static String password = "example_password";
    private static String host = "example_host";
    private static String database = "example_db";
    private static int port = 8084;

    private Connection con;

    public BOOKDB() {
    }


    /**
     * Place your initialization code inside if required.
     *
     * <p>
     * This function will be called before all other operations. If your
     * implementation need initialization , necessary operations should be done
     * inside this function. For example, you can set your connection to the
     * database server inside this function.
     */
    public void initialize() {
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.con =  DriverManager.getConnection(url, this.user, this.password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Should create the necessary tables when called.
     *
     * @return the number of tables that are created successfully.
     */
    public int createTables() {

        int result;
        int numberofTablesInserted = 0;
        List<String> createTableQueries = new ArrayList<String>();

        String queryForauthorTable = "create table author (" +
                "author_id int," +
                "author_name VARCHAR(60)," +
                "primary key (author_id))";
        createTableQueries.add(queryForauthorTable);

        String queryForpublisherTable = "create table publisher (" +
                "publisher_id int," +
                "publisher_name VARCHAR(50)," +
                "primary key (publisher_id))";
        createTableQueries.add(queryForpublisherTable);

        String queryForbookTable = "create table book (" +
                "isbn char(13)," +
                "book_name VARCHAR(120)," +
                "publisher_id int," +
                "first_publish_year char(4)," +
                "page_count int," +
                "category VARCHAR(25)," +
                "rating float," +
                "primary key (isbn)," +
                "foreign key (publisher_id) references publisher(publisher_id))";
        createTableQueries.add(queryForbookTable);

        String queryForauthor_ofTable = "create table author_of (" +
                "isbn char(13)," +
                "author_id int," +
                "primary key (isbn, author_id)," +
                "foreign key (isbn) references book(isbn)," +
                "foreign key (author_id) references author(author_id))";
        createTableQueries.add(queryForauthor_ofTable);

        String queryForphw1Table = "create table phw1 (" +
                "isbn char(13)," +
                "book_name VARCHAR(120)," +
                "rating float," +
                "primary key (isbn))";
        createTableQueries.add(queryForphw1Table);

        try {
            Statement statement = this.con.createStatement();

            for(int i=0; i< createTableQueries.size(); i++) {
                result = statement.executeUpdate(createTableQueries.get(i));
                //System.out.println(result);
                numberofTablesInserted++;
            }

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberofTablesInserted;
    }


    /**
     * Should drop the tables if exists when called.
     *
     * @return the number of tables are dropped successfully.
     */
    public int dropTables() {

        int result;
        int numberofTablesDropped = 0;
        List<String> dropTableQueries = new ArrayList<String>();

        String queryForDropphw1Table = "drop table if exists phw1";
        dropTableQueries.add(queryForDropphw1Table);

        String queryForDropauthor_ofTable = "drop table if exists author_of";
        dropTableQueries.add(queryForDropauthor_ofTable);

        String queryForDropbookTable = "drop table if exists book";
        dropTableQueries.add(queryForDropbookTable);

        String queryForDroppublisherTable = "drop table if exists publisher";
        dropTableQueries.add(queryForDroppublisherTable);

        String queryForDropauthorTable = "drop table if exists author";
        dropTableQueries.add(queryForDropauthorTable);

        try {
            Statement statement = this.con.createStatement();

            for(int i=0; i<dropTableQueries.size();i++) {
                result = statement.executeUpdate(dropTableQueries.get(i));
                numberofTablesDropped++;
                //System.out.println(result);
            }

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numberofTablesDropped;
    }



    /**
     * Should insert an array of Author into the database.
     *
     * @return Number of rows inserted successfully.
     */
    public int insertAuthor(Author[] authors) {
        int result = 0;
        int numberOfRowsInserted = 0;

        try {
            Statement st = this.con.createStatement();

            for(int i=0; i<authors.length; i++) {
                String query = "insert into author values (" +
                        authors[i].getAuthor_id()+ ",'" +
                        authors[i].getAuthor_name().replaceAll("'","''") + "')";

                numberOfRowsInserted++;
                result = st.executeUpdate(query);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfRowsInserted;
    }


    /**
     * Should insert an array of Book into the database.
     *
     * @return Number of rows inserted successfully.
     */
    public int insertBook(Book[] books) {
        int result = 0;
        int numberOfRowsInserted = 0;

        try {
            Statement st = this.con.createStatement();

            for(int i=0; i<books.length; i++) {
                String query = "insert into book values ('" +
                        books[i].getIsbn().replaceAll("'","''")+ "','" +
                        books[i].getBook_name().replaceAll("'","''")+ "'," +
                        books[i].getPublisher_id()+ ",'" +
                        books[i].getFirst_publish_year().replaceAll("'","''")+ "'," +
                        books[i].getPage_count()+ ",'" +
                        books[i].getCategory().replaceAll("'","''")+ "'," +
                        books[i].getRating() + ")";

                numberOfRowsInserted++;
                result = st.executeUpdate(query);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfRowsInserted;
    }


    /**
     * Should insert an array of Publisher into the database.
     *
     * @return Number of rows inserted successfully.
     */
    public int insertPublisher(Publisher[] publishers) {
        int result = 0;
        int numberOfRowsInserted = 0;

        try {
            Statement st = this.con.createStatement();

            for(int i=0; i<publishers.length; i++) {
                String query = "insert into publisher values (" +
                        publishers[i].getPublisher_id()+ ",'" +
                        publishers[i].getPublisher_name().replaceAll("'","''") + "')";

                numberOfRowsInserted++;
                result = st.executeUpdate(query);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfRowsInserted;
    }


    /**
     * Should insert an array of Author_of into the database.
     *
     * @return Number of rows inserted successfully.
     */
    public int insertAuthor_of(Author_of[] author_ofs) {
        int result = 0;
        int numberOfRowsInserted = 0;

        try {
            Statement st = this.con.createStatement();

            for(int i=0; i<author_ofs.length; i++) {
                String query = "insert into author_of values ('" +
                        author_ofs[i].getIsbn().replaceAll("'","''")+ "'," +
                        author_ofs[i].getAuthor_id() + ")";

                numberOfRowsInserted++;
                result = st.executeUpdate(query);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numberOfRowsInserted;
    }


    /**
     * Should return isbn, first_publish_year, page_count and publisher_name of
     * the books which have the maximum number of pages.
     * @return QueryResult.ResultQ1[]
     */
    public QueryResult.ResultQ1[] functionQ1() {
        ResultSet rs;
        List<QueryResult.ResultQ1> totalResult = new ArrayList<QueryResult.ResultQ1>();

        String query = "select B.isbn, B.first_publish_year, B.page_count, P.publisher_name " +
                "from book B, publisher P " +
                "where B.publisher_id = P.publisher_id AND B.page_count = (" +
                "select MAX(B2.page_count) from book B2) " +
                "order by B.isbn;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                String rs_isbn = rs.getString("isbn");
                String rs_firstPublishYear = rs.getString("first_publish_year");
                int rs_pageCount = rs.getInt("page_count");
                String rs_publisherName = rs.getString("publisher_name");

                QueryResult.ResultQ1 rowResult = new QueryResult.ResultQ1(rs_isbn, rs_firstPublishYear, rs_pageCount, rs_publisherName);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ1[] returnedArray = totalResult.toArray(new QueryResult.ResultQ1[totalResult.size()]);
        return returnedArray;
    }

    /**
     * For the publishers who have published books that were co-authored by both
     * of the given authors(author1 and author2); list publisher_id(s) and average
     * page_count(s)  of all the books these publishers have published.
     * @param author_id1
     * @param author_id2
     * @return QueryResult.ResultQ2[]
     */

    public QueryResult.ResultQ2[] functionQ2(int author_id1, int author_id2) {
        ResultSet rs;
        List<QueryResult.ResultQ2> totalResult = new ArrayList<QueryResult.ResultQ2>();

        String query = "SELECT P3.publisher_id, AVG(B3.page_count) AS avg_page_count "+
        "FROM (SELECT P2.publisher_id "+
                "FROM (SELECT B.isbn "+
                        "FROM author_of AO, book B "+
                        "WHERE AO.author_id =" + author_id1 + " AND AO.isbn = B.isbn AND B.isbn IN ("+
                        "SELECT B1.isbn "+
                        "FROM author_of AO1, book B1 "+
                        "WHERE AO1.author_id =" + author_id2 + " AND AO1.isbn = B1.isbn)) R, book B2, publisher P2 "+
                "WHERE R.isbn = B2.isbn AND B2.publisher_id = P2.publisher_id) P3, book B3 "+
        "WHERE P3.publisher_id = B3.publisher_id "+
        "GROUP BY P3.publisher_id "+
        "ORDER BY P3.publisher_id;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                int rs_publisherId = rs.getInt("publisher_id");
                double rs_avgPageCount = rs.getDouble("avg_page_count");

                QueryResult.ResultQ2 rowResult = new QueryResult.ResultQ2(rs_publisherId, rs_avgPageCount);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ2[] returnedArray = totalResult.toArray(new QueryResult.ResultQ2[totalResult.size()]);
        return returnedArray;
    }

    /**
     * List book_name, category and first_publish_year of the earliest
     * published book(s) of the author(s) whose author_name is given.
     * @param author_name
     * @return QueryResult.ResultQ3[]
     */

    public QueryResult.ResultQ3[] functionQ3(String author_name) {
        ResultSet rs;
        List<QueryResult.ResultQ3> totalResult = new ArrayList<QueryResult.ResultQ3>();

        String query = "SELECT B1.book_name, B1.category, B1.first_publish_year "+
                "FROM (SELECT A.author_id, MIN(B.first_publish_year) AS min_first_publish_year "+
                        "FROM book B, author_of AO, author A "+
                        "WHERE A.author_name = '" + author_name + "' AND A.author_id = AO.author_id AND AO.isbn = B.isbn "+
                        "GROUP BY A.author_id) X, book B1, author_of AO1 "+
        "WHERE X.author_id = AO1.author_id AND AO1.isbn = B1.isbn AND B1.first_publish_year = X.min_first_publish_year "+
        "ORDER BY B1.book_name, B1.category, B1.first_publish_year;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                String rs_bookName = rs.getString("book_name");
                String rs_category = rs.getString("category");
                String rs_firstPublishYear = rs.getString("first_publish_year");

                QueryResult.ResultQ3 rowResult = new QueryResult.ResultQ3(rs_bookName, rs_category, rs_firstPublishYear);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ3[] returnedArray = totalResult.toArray(new QueryResult.ResultQ3[totalResult.size()]);
        return returnedArray;
    }

    /**
     * For publishers whose name contains at least 3 words
     * (i.e., "Koc Universitesi Yayinlari"), have published at least 3 books
     * and average rating of their books are greater than(>) 3;
     * list their publisher_id(s) and distinct category(ies) they have published.
     * PS: You may assume that each word in publisher_name is seperated by a space.
     * @return QueryResult.ResultQ4[]
     */
    public QueryResult.ResultQ4[] functionQ4() {
        ResultSet rs;
        List<QueryResult.ResultQ4> totalResult = new ArrayList<QueryResult.ResultQ4>();

        String query = "SELECT DISTINCT B5.publisher_id, B5.category "+
        "FROM (SELECT P.publisher_id "+
                "FROM publisher P, book B "+
                "WHERE P.publisher_name LIKE '_% _% _%' AND P.publisher_id = B.publisher_id "+
        "GROUP BY P.publisher_id "+
        "HAVING COUNT(*) >= 3) X, (SELECT P1.publisher_id, AVG(B1.rating) AS avg_ratings "+
        "FROM publisher P1, book B1 "+
        "WHERE P1.publisher_name LIKE '_% _% _%' AND P1.publisher_id = B1.publisher_id "+
        "GROUP BY P1.publisher_id "+
        "HAVING AVG(B1.rating) > 3) Y, book B5 "+
        "WHERE X.publisher_id = Y.publisher_id AND Y.publisher_id = B5.publisher_id "+
        "ORDER BY B5.publisher_id, B5.category;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                int rs_publisherId = rs.getInt("publisher_id");
                String rs_category = rs.getString("category");

                QueryResult.ResultQ4 rowResult = new QueryResult.ResultQ4(rs_publisherId, rs_category);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ4[] returnedArray = totalResult.toArray(new QueryResult.ResultQ4[totalResult.size()]);
        return returnedArray;
    }

    /**
     * List author_id and author_name of the authors who have worked with
     * all the publishers that the given author_id has worked.
     * @param author_id
     * @return QueryResult.ResultQ5[]
     */

    public QueryResult.ResultQ5[] functionQ5(int author_id) {
        ResultSet rs;
        List<QueryResult.ResultQ5> totalResult = new ArrayList<QueryResult.ResultQ5>();

        String query = "SELECT A.author_id, A.author_name "+
        "FROM author A "+
        "WHERE NOT EXISTS ( "+
                "SELECT P1.publisher_id "+
                "FROM author_of AO1, book B1, publisher P1 "+
                "WHERE AO1.author_id =" + author_id + " AND AO1.isbn = B1.isbn AND B1.publisher_id = P1.publisher_id AND P1.publisher_id NOT IN ("+
                "SELECT P2.publisher_id "+
                "FROM author_of AO2, book B2, publisher P2 "+
                "WHERE A.author_id = AO2.author_id AND AO2.isbn = B2.isbn AND B2.publisher_id = P2.publisher_id)) "+
        "ORDER BY A.author_id;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                int rs_authorId = rs.getInt("author_id");
                String rs_authorName = rs.getString("author_name");

                QueryResult.ResultQ5 rowResult = new QueryResult.ResultQ5(rs_authorId, rs_authorName);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ5[] returnedArray = totalResult.toArray(new QueryResult.ResultQ5[totalResult.size()]);
        return returnedArray;
    }

    /**
     * List author_name(s) and isbn(s) of the book(s) written by "Selective" authors.
     * "Selective" authors are those who have worked with publishers that have
     * published their books only.(i.e., they haven't published books of
     * different authors)
     * @return QueryResult.ResultQ6[]
     */

    public QueryResult.ResultQ6[] functionQ6() {
        ResultSet rs;
        List<QueryResult.ResultQ6> totalResult = new ArrayList<QueryResult.ResultQ6>();

        String query = "SELECT DISTINCT AO4.author_id, AO4.isbn "+
        "FROM (SELECT AO2.author_id "+
                "FROM author_of AO2, book B2 "+
                "WHERE AO2.isbn = B2.isbn AND B2.publisher_id IN "+
                        "(SELECT P.publisher_id "+
                                "FROM author_of AO, book B, publisher P "+
                                "WHERE AO.isbn = B.isbn AND B.publisher_id = P.publisher_id "+
                                "GROUP BY P.publisher_id "+
                                "HAVING COUNT(DISTINCT AO.author_id) = 1) AND AO2.author_id NOT IN ("+
                "SELECT AO3.author_id "+
                "FROM author_of AO3, book B3 "+
                "WHERE AO3.isbn = B3.isbn AND B3.publisher_id IN "+
                        "(SELECT P1.publisher_id "+
                                "FROM author_of AO1, book B1, publisher P1 "+
                                "WHERE AO1.isbn = B1.isbn AND B1.publisher_id = P1.publisher_id "+
                                "GROUP BY P1.publisher_id "+
                                "HAVING COUNT(DISTINCT AO1.author_id) <> 1))) X, author_of AO4 "+
        "WHERE X.author_id = AO4.author_id "+
        "ORDER BY AO4.author_id, AO4.isbn;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                int rs_authorId = rs.getInt("author_id");
                String rs_isbn = rs.getString("isbn");

                QueryResult.ResultQ6 rowResult = new QueryResult.ResultQ6(rs_authorId, rs_isbn);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ6[] returnedArray = totalResult.toArray(new QueryResult.ResultQ6[totalResult.size()]);
        return returnedArray;
    }

    /**
     * List publisher_id and publisher_name of the publishers who have published
     * at least 2 books in  'Roman' category and average rating of their books
     * are more than (>) the given value.
     * @param rating
     * @return QueryResult.ResultQ7[]
     */
    public QueryResult.ResultQ7[] functionQ7(double rating) {
        ResultSet rs;
        List<QueryResult.ResultQ7> totalResult = new ArrayList<QueryResult.ResultQ7>();

        String query = "SELECT P2.publisher_id, P2.publisher_name "+
        "FROM (SELECT P1.publisher_id "+
                "FROM publisher P1, book B1 "+
                "WHERE P1.publisher_id = B1.publisher_id "+
                "GROUP BY P1.publisher_id "+
                "HAVING AVG(B1.rating) > " + rating + ") X, "+
                "(SELECT P.publisher_id "+
        "FROM publisher P, book B "+
        "WHERE P.publisher_id = B.publisher_id AND B.category = 'Roman' "+
        "GROUP BY P.publisher_id "+
        "HAVING COUNT(*) >= 2) Y, publisher P2 "+
        "WHERE X.publisher_id = Y.publisher_id AND Y.publisher_id = P2.publisher_id "+
        "ORDER BY P2.publisher_id;";

        try {
            Statement st = this.con.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()) {
                int rs_publisherId = rs.getInt("publisher_id");
                String rs_publisherName = rs.getString("publisher_name");

                QueryResult.ResultQ7 rowResult = new QueryResult.ResultQ7(rs_publisherId, rs_publisherName);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ7[] returnedArray = totalResult.toArray(new QueryResult.ResultQ7[totalResult.size()]);
        return returnedArray;
    }

    /**
     * Some of the books  in the store have been published more than once:
     * although they have same names(book\_name), they are published with different
     * isbns. For each  multiple copy of these books, find the book_name(s) with the
     * lowest rating for each book_name  and store their isbn, book_name and
     * rating into phw1 table using a single BULK insertion query.
     * If there exists more than 1 with the lowest rating, then store them all.
     * After the bulk insertion operation, list isbn, book_name and rating of
     * all rows in phw1 table.
     * @return QueryResult.ResultQ8[]
     */

    public QueryResult.ResultQ8[] functionQ8() {
        ResultSet rs;
        List<QueryResult.ResultQ8> totalResult = new ArrayList<QueryResult.ResultQ8>();

        //Calling bulk insertion method firstly
        functionQ8bulkInsertion();

        // Then return results
        try {
            Statement st = this.con.createStatement();

            String resultQuery = "SELECT P.isbn, P.book_name, P.rating "+
            "FROM phw1 P "+
            "ORDER BY P.isbn;";

            rs = st.executeQuery(resultQuery);

            while(rs.next()) {
                String rs_isbn = rs.getString("isbn");
                String rs_bookName = rs.getString("book_name");
                Double rs_rating = rs.getDouble("rating");

                QueryResult.ResultQ8 rowResult = new QueryResult.ResultQ8(rs_isbn, rs_bookName, rs_rating);
                totalResult.add(rowResult);
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        QueryResult.ResultQ8[] returnedArray = totalResult.toArray(new QueryResult.ResultQ8[totalResult.size()]);
        return returnedArray;
    }


    private void functionQ8bulkInsertion() {
        int result = 0;

        try {
            Statement st = this.con.createStatement();

            String bulkInsertQuery = "INSERT INTO phw1 (isbn, book_name, rating) "+
                    "SELECT B1.isbn, B1.book_name, B1.rating "+
                    "FROM (SELECT B.book_name, MIN(B.rating) AS min_rating "+
                    "FROM book B "+
                    "GROUP BY B.book_name "+
                    "HAVING COUNT(*) > 1) X, book B1 "+
                    "WHERE B1.book_name = X.book_name AND B1.rating = X.min_rating;";

            result = st.executeUpdate(bulkInsertQuery);

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * For the books that contain the given keyword anywhere in their names,
     * increase their ratings by one.
     * Please note that, the maximum rating cannot be more than 5,
     * therefore if the previous rating is greater than 4, do not update the
     * rating of that book.
     * @param keyword
     * @return sum of the ratings of all books
     */

    public double functionQ9(String keyword) {

        ResultSet rs;
        double result = 0;

        //Update operation firstly
        functionQ9UpdateOperation(keyword);

        //Then return result
        try {
            Statement st = this.con.createStatement();

            String query = "SELECT SUM(B1.rating) AS total_rating "+
            "FROM book B1;";

            rs = st.executeQuery(query);

            while(rs.next()) {
                result = rs.getDouble("total_rating");
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }



    private void functionQ9UpdateOperation(String keyword) {
        int result = 0;

        try {
            Statement st = this.con.createStatement();

            String updateQuery = "UPDATE book B "+
            "SET B.rating = B.rating + 1 "+
            "WHERE B.book_name LIKE '%" + keyword + "%' AND B.rating <= 4;";

            result = st.executeUpdate(updateQuery);

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Delete publishers in publisher table who haven't published a book yet.
     * @return count of all rows of the publisher table after delete operation.
     */
    public int function10() {

        ResultSet rs;
        int result = 0;

        //Delete operation firstly
        functionQ10DeleteOperation();

        //Then return result
        try {
            Statement st = this.con.createStatement();

            String query = "SELECT COUNT(*) AS count_rows "+
            "FROM publisher P2;";

            rs = st.executeQuery(query);

            while(rs.next()) {
                result = rs.getInt("count_rows");
            }

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    private void functionQ10DeleteOperation() {

        int result = 0;

        try {
            Statement st = this.con.createStatement();

            String deleteQuery = "DELETE FROM publisher P "+
            "WHERE P.publisher_id NOT IN ("+
                    "SELECT X.publisher_id "+
                    "FROM (SELECT * "+
                            "FROM publisher) X, book B1 "+
                    "WHERE X.publisher_id = B1.publisher_id);";

            result = st.executeUpdate(deleteQuery);

            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
