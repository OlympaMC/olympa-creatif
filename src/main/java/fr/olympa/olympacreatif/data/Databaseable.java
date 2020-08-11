package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;

public interface Databaseable {

	Object fromDbFormat(ResultSet data);
}
