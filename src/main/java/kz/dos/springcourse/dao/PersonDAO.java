package kz.dos.springcourse.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import kz.dos.springcourse.models.Person;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PersonDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> index() {
        return jdbcTemplate.query("SELECT * FROM Person", new BeanPropertyRowMapper<>(Person.class));
    }

    public Optional<Person> show(String email) {
        return jdbcTemplate.query("SELECT * FROM Person WHERE email=?",
                        new Object[]{email}, new BeanPropertyRowMapper<>(Person.class))
                        .stream().findAny();
    }

    public Person show(int id) {
        return jdbcTemplate.query("SELECT * FROM Person WHERE id=?",
                        new Object[]{id}, new BeanPropertyRowMapper<>(Person.class))
                        .stream().findAny().orElse(null);
    }

    public void save(Person person) {
        jdbcTemplate.update("INSERT INTO Person(name, age, email, address) VALUES (?, ?, ?, ?)",
                person.getName(), person.getAge(), person.getEmail(), person.getAddress());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("UPDATE Person SET name=?, age=?, email=?, address=? WHERE id=?",
                updatedPerson.getName(), updatedPerson.getAge(), updatedPerson.getEmail(), updatedPerson.getAddress(), id);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM Person WHERE id=?", id);
    }

    /////////////////
    /// just testing batch update
    /////////////////

    public void testMultipleUpdate(){
        List<Person> people = create200People();
        long before = System.currentTimeMillis();

        for(Person person : people){
            jdbcTemplate.update("INSERT INTO Person(id, name, age, email, address) VALUES (?, ?, ?, ?, ?)",
                    person.getId(), person.getName(), person.getAge(), person.getEmail(), person.getAddress());
        }

        long after = System.currentTimeMillis();
        System.out.println("Time: " + (after - before));

    }

    public void testBatchUpdate(){
        List<Person> people = create200People();
        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("INSERT INTO Person(id, name, age, email, address) VALUES (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, people.get(i).getId());
                        ps.setString(2, people.get(i).getName());
                        ps.setInt(3, people.get(i).getAge());
                        ps.setString(4, people.get(i).getEmail());
                        ps.setString(5, people.get(i).getAddress());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });

        long after = System.currentTimeMillis();
        System.out.println("Time: " + (after - before));
    }

    private List<Person> create200People() {
        List<Person> people = new ArrayList<Person>();
        for(int i = 1; i <= 200; i++){
            Person person = new Person(i, "Name" + i, 30, "email" + i + "@gmail.com", "some address");
            people.add(person);
        }
        return people;
    }
}