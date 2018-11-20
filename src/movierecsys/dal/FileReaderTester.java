/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movierecsys.dal;

import movierecsys.dal.file.RatingDAO;
import movierecsys.dal.file.MovieDAO;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import movierecsys.be.Movie;
import movierecsys.be.Rating;
import movierecsys.be.User;
import movierecsys.dal.file.UserDAO;

/**
 *
 * @author pgn
 */
public class FileReaderTester
{

    /**
     * Example method. This is the code I used to create the users.txt files.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        //mitigateMovies();
//        mitigateUsers();
        mitigateRatings();
    }

    public static void mitigateUsers()
    {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("10.176.111.31");
        ds.setDatabaseName("mrs");
        ds.setUser("CS2018A_40");
        ds.setPassword("CS2018A_40");

        List<User> users = new UserDAO().getAllUsers();

        try (Connection con = ds.getConnection())
        {
            Statement statement = con.createStatement();
            int counter = 0;
            for (User user : users)
            {
                String sql = "INSERT INTO User (id,name) VALUES("
                        + user.getId() + ",'"
                        + user.getName() + "');";
                statement.addBatch(sql);
                counter++;
                if (counter % 10000 == 0)
                {
                    statement.executeBatch();
                    System.out.println("Added " + counter + " users.");
                }
            }
            statement.executeBatch();
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Untested batch insert. We'll see how it goes tomorrow.
     *
     * @throws IOException
     */
    public static void mitigateRatings() throws IOException
    {
        List<Rating> allRatings = new RatingDAO().getAllRatings();
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("10.176.111.31");
        ds.setDatabaseName("mrs");
        ds.setUser("CS2018A_40");
        ds.setPassword("CS2018A_40");
        try (Connection con = ds.getConnection())
        {
            Statement st = con.createStatement();
            int counter = 0;
            long lastTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            for (Rating rating : allRatings)
            {

                String sql = "INSERT INTO Rating (movieId, userId, rating) VALUES ("
                        + rating.getMovie() + ","
                        + rating.getUser() + ","
                        + rating.getRating()
                        + ");";
                st.addBatch(sql);
                counter++;
                if (counter % 10000 == 0)
                {

                    st.executeBatch();
                    currentTime = System.currentTimeMillis();
                    long timePerInsert = (currentTime - lastTime) / 10000;
                    System.out.println("Millis per insert: " + timePerInsert);
                    System.out.println("Added " + counter + " ratings.");
                    lastTime = currentTime;
                }
            }
            st.executeBatch();
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void mitigateMovies() throws IOException
    {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("10.176.111.31");
        ds.setDatabaseName("mrs");
        ds.setUser("CS2018A_40");
        ds.setPassword("CS2018A_40");

        MovieDAO mvDao = new MovieDAO();
        List<Movie> movies = mvDao.getAllMovies();

        try (Connection con = ds.getConnection())
        {
            Statement statement = con.createStatement();

            for (Movie movie : movies)
            {
                String sql = "INSERT INTO Movie (id,year,title) VALUES("
                        + movie.getId() + ","
                        + movie.getYear() + ",'"
                        + movie.getTitle().replace("'", "") + "');";
                System.out.println(sql);
                int i = statement.executeUpdate(sql);
                // INSERT INTO Movie (id,year,title) VALUES (1,2018,Venom);
                System.out.println("Affected row = " + i);
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void createRafFriendlyRatingsFile() throws IOException
    {
        String target = "data/user_ratings";
        RatingDAO ratingDao = new RatingDAO();
        List<Rating> all = ratingDao.getAllRatings();

        try (RandomAccessFile raf = new RandomAccessFile(target, "rw"))
        {
            for (Rating rating : all)
            {
                raf.writeInt(rating.getMovie());
                raf.writeInt(rating.getUser());
                raf.writeInt(rating.getRating());
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

}
