package com.johannesbrodwall.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class ConfiguredDataSourceTest {

    private ConfiguredDataSource dataSource = new ConfiguredDataSource(null, null);

    @Test
    public void itShouldDetermineUrlForPostgresql() throws URISyntaxException {
        assertThat(dataSource.getJdbcUrlFromDbUrl(new URI("postgres://foo:foo@heroku.com:5432/hellodb")))
            .isEqualTo("jdbc:postgresql://heroku.com:5432/hellodb");
    }

    @Test
    public void itShouldDetermineUrlForMysql() throws URISyntaxException {
        assertThat(dataSource.getJdbcUrlFromDbUrl(new URI("mysql://foo:foo@us-cdbr-east.cleardb.com/somedb?reconnect=true")))
            .isEqualTo("jdbc:mysql://us-cdbr-east.cleardb.com/somedb");
    }

    @Test
    public void itShouldDetermineUrlForMysqlWithPort() throws URISyntaxException {
        assertThat(dataSource.getJdbcUrlFromDbUrl(new URI("mysql://foo:foo@dbhost:3307/somedb")))
        .isEqualTo("jdbc:mysql://dbhost:3307/somedb");
    }

    @Test
    public void itShouldDetermineUrlForOracle() throws URISyntaxException {
        assertThat(dataSource.getJdbcUrlFromDbUrl(new URI("oracle://foo:foo@foo.bar.com:1521/somedb")))
            .isEqualTo("jdbc:oracle:thin:@foo.bar.com:1521:somedb");
    }
}
