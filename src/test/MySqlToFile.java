package test;

import java.io.IOException;
import java.util.List;

import org.bson.Document;

import com.file.handling.ErrorWriter;
import com.file.handling.FileExporter;
import com.utils.ErrorUtils;

/**
 * 
 * @author Sushil Paudel
 *
 */
public class MySqlToFile{

	/*public String exportFile(List<Document> documents, String tableName, int counter) throws IOException {
		FileExporter exporter = new FileExporter(tableName, counter);

		for (Document document : documents) {

			System.out.println(document.toJson());

			try {

//				exporter.write(document.toJson());
//				exporter.write("\n");

				// throw new Exception("test export throw");

			} catch (Exception ex) {
				ErrorUtils.processError(tableName, document, ErrorUtils.ERROR_TYPE_EXPORT);
			}

		}

		exporter.close();
		return exporter.getFilePath();
	}*/

}
