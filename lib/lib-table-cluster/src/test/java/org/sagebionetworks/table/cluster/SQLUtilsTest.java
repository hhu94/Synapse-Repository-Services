package org.sagebionetworks.table.cluster;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.dbo.dao.table.TableModelTestUtils;
import org.sagebionetworks.repo.model.table.AbstractDouble;
import org.sagebionetworks.repo.model.table.AnnotationDTO;
import org.sagebionetworks.repo.model.table.AnnotationType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.EntityField;
import org.sagebionetworks.repo.model.table.IdRange;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableConstants;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.table.cluster.SQLUtils.TableType;
import org.sagebionetworks.table.cluster.utils.TableModelUtils;
import org.sagebionetworks.table.model.Grouping;
import org.sagebionetworks.table.model.SparseChangeSet;
import org.sagebionetworks.table.model.SparseRow;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.google.common.collect.Lists;


public class SQLUtilsTest {
	
	@Mock
	PreparedStatement mockPreparedStatement;
	
	List<ColumnModel> simpleSchema;
	
	Map<Long, ColumnModel> schemaIdToModelMap;
	
	AnnotationDTO annotationDto;
	
	boolean isFirst;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		simpleSchema = new LinkedList<ColumnModel>();
		ColumnModel col = new ColumnModel();
		col.setColumnType(ColumnType.INTEGER);
		col.setDefaultValue(null);
		col.setId("456");
		col.setName("colOne");
		simpleSchema.add(col);
		col = new ColumnModel();
		col.setColumnType(ColumnType.STRING);
		col.setDefaultValue(null);
		col.setId("789");
		col.setMaximumSize(300L);
		col.setName("coTwo");
		simpleSchema.add(col);
		col = new ColumnModel();
		col.setColumnType(ColumnType.STRING);
		col.setDefaultValue(null);
		col.setId("123");
		col.setMaximumSize(150L);
		col.setName("colThree");
		simpleSchema.add(col);
		
		schemaIdToModelMap = TableModelUtils.createIDtoColumnModelMap(simpleSchema);
		
		annotationDto = new AnnotationDTO();
		annotationDto.setEntityId(123L);
		annotationDto.setType(AnnotationType.STRING);
		annotationDto.setKey("someKey");
		annotationDto.setValue("someString");
		
		isFirst = true;
	}

	
	@Test
	public void testparseValueForDBLong(){
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.INTEGER, "123");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBFileHandle(){
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.FILEHANDLEID, "123");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBUserId(){
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.USERID, "123");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBEntityId() {
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.ENTITYID, "syn123");
		assertEquals(expected, objectValue);
	}

	@Test
	public void testparseValueForDBDate() {
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.DATE, "123");
		assertEquals(expected, objectValue);
	}

	@Test
	public void testparseValueForDBDateString() {
		Long expected = new Long(123);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.DATE, "1970-1-1 00:00:00.123");
		assertEquals(expected, objectValue);
	}

	@Test
	public void testparseValueForDBDouble(){
		Double expected = new Double(123.456);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.DOUBLE, "123.456");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBBooleanTrue(){
		Boolean expected = new Boolean(true);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.BOOLEAN, "true");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBBooleanFalse(){
		Boolean expected = new Boolean(false);
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.BOOLEAN, "false");
		assertEquals(expected, objectValue);
	}
	
	@Test
	public void testparseValueForDBLargeText(){
		String expected = "this is some text";
		Object objectValue = SQLUtils.parseValueForDB(ColumnType.LARGETEXT, "this is some text");
		assertEquals(expected, objectValue);
	}
	

	@Test
	public void testGetTableNameForId(){
		assertEquals("T123", SQLUtils.getTableNameForId("syn123", TableType.INDEX));
		assertEquals("T123S", SQLUtils.getTableNameForId("syn123", TableType.STATUS));
	}
	
	@Test
	public void testGetColumnNameForId(){
		assertEquals("_C456_", SQLUtils.getColumnNameForId("456"));
	}
	
	@Test
	public void testGetColumnNames() {
		assertEquals("", createColNames());
		assertEquals("_C0_", createColNames(ColumnType.STRING));
		assertEquals("_C0_,_DBL_C0_", createColNames(ColumnType.DOUBLE));
		assertEquals("_C0_,_DBL_C0_,_C1_", createColNames(ColumnType.DOUBLE, ColumnType.STRING));
		assertEquals("_C0_,_C1_,_DBL_C1_", createColNames(ColumnType.STRING, ColumnType.DOUBLE));
		assertEquals("_C0_,_C1_,_DBL_C1_,_C2_,_DBL_C2_", createColNames(ColumnType.STRING, ColumnType.DOUBLE, ColumnType.DOUBLE));
		assertEquals("_C0_,_C1_,_DBL_C1_,_C2_", createColNames(ColumnType.STRING, ColumnType.DOUBLE, ColumnType.STRING));
		assertEquals("_C0_,_DBL_C0_,_C1_,_C2_,_DBL_C2_", createColNames(ColumnType.DOUBLE, ColumnType.STRING, ColumnType.DOUBLE));
	}

	private String createColNames(ColumnType... types) {
		List<ColumnModel> models = Lists.newArrayList();
		for (int i = 0; i < types.length; i++) {
			ColumnType columnType = types[i];
			models.add(TableModelTestUtils.createColumn((long) i, "x", columnType));
		}
		return StringUtils.join(Lists.newArrayList(SQLUtils.getColumnNames(models)), ",");
	}

	@Test
	public void testAppendDoubleCase() {
		StringBuilder sb = new StringBuilder();
		SQLUtils.appendDoubleCase("3", sb);
		assertEquals("CASE WHEN _DBL_C3_ IS NULL THEN _C3_ ELSE _DBL_C3_ END", sb.toString());
	}
	
	@Test
	public void testBuildCreateOrUpdateRowSQL(){
		List<ColumnModel> newSchema = helperCreateColumnsWithIds("0","2","4");
		String result = SQLUtils.buildCreateOrUpdateRowSQL(newSchema, "syn123");
		String expected = "INSERT INTO T123 (ROW_ID, ROW_VERSION, _C0_, _C2_, _C4_) VALUES ( :bRI, :bRV, :_C0_, :_C2_, :_C4_) ON DUPLICATE KEY UPDATE ROW_VERSION = VALUES(ROW_VERSION), _C0_ = VALUES(_C0_), _C2_ = VALUES(_C2_), _C4_ = VALUES(_C4_)";
		assertEquals(expected, result);
	}
	
	@Test
	public void testBindParametersForCreateOrUpdate(){
		List<ColumnModel> oldSchema = helperCreateColumnsWithIds("0","2","4");
		SparseChangeSet set = new SparseChangeSet("syn123", oldSchema);
		for(int i=0; i<2; i++){
			SparseRow row = set.addEmptyRow();
			row.setRowId(new Long(i));
			row.setVersionNumber(3L);
			row.setCellValue("0", "111"+i);
			row.setCellValue("2", "222"+i);
			row.setCellValue("4", "333"+i);
		}
		Grouping grouping = set.groupByValidValues().iterator().next();
		
		// bind!
		SqlParameterSource[] results = SQLUtils.bindParametersForCreateOrUpdate(grouping);
		assertNotNull(results);
		assertEquals("There should be one mapping for each row in the batch",2, results.length);
		// First row
		assertEquals(new Long(0), results[0].getValue(SQLUtils.ROW_ID_BIND));
		assertEquals(new Long(3), results[0].getValue(SQLUtils.ROW_VERSION_BIND));
		assertEquals(new Long(1110), results[0].getValue("_C0_"));
		assertEquals(new Long(2220), results[0].getValue("_C2_"));
		assertEquals(new Long(3330), results[0].getValue("_C4_"));
		// second
		assertEquals(new Long(1111), results[1].getValue("_C0_"));
		assertEquals(new Long(2221), results[1].getValue("_C2_"));
		assertEquals(new Long(3331), results[1].getValue("_C4_"));
	}
	
	
	@Test
	public void testBindParametersForCreateOrUpdateAllTypes(){
		List<ColumnModel> newSchema = TableModelTestUtils.createOneOfEachType();
		RowSet set = new RowSet();
		set.setRows(TableModelTestUtils.createRows(newSchema, 3));
		set.setHeaders(TableModelUtils.getSelectColumns(newSchema));
		set.setTableId("syn123");
		IdRange range = new IdRange();
		range.setMinimumId(100L);
		range.setMaximumId(200L);
		range.setVersionNumber(3L);
		TableModelTestUtils.assignRowIdsAndVersionNumbers(set, range);
		SparseChangeSet sparseSet = TableModelUtils.createSparseChangeSet(set, newSchema);
		Grouping grouping = sparseSet.groupByValidValues().iterator().next();
		// bind!
		SqlParameterSource[] results = SQLUtils.bindParametersForCreateOrUpdate(grouping);
		assertNotNull(results);
		assertEquals("There should be one mapping for each row in the batch",3, results.length);
		// First row
		assertEquals(new Long(100), results[0].getValue(SQLUtils.ROW_ID_BIND));
		assertEquals(new Long(3), results[0].getValue(SQLUtils.ROW_VERSION_BIND));
		assertEquals("string0", results[0].getValue("_C0_"));
		assertEquals(new Double(341003.12), results[0].getValue("_C1_"));
		assertEquals(null, results[0].getValue("_DBL_C1_"));
		assertEquals(new Long(203000), results[0].getValue("_C2_"));
		assertEquals(new Boolean(false), results[0].getValue("_C3_"));
		assertEquals(new Long(404000), results[0].getValue("_C4_"));
		assertEquals(new Long(505000), results[0].getValue("_C5_"));
		assertEquals(new Long(606000), results[0].getValue("_C6_"));
		// second
		assertEquals(new Long(101), results[1].getValue(SQLUtils.ROW_ID_BIND));
		assertEquals(new Long(3), results[1].getValue(SQLUtils.ROW_VERSION_BIND));
		assertEquals("string1", results[1].getValue("_C0_"));
		assertEquals(new Double(341006.53), results[1].getValue("_C1_"));
		assertEquals(null, results[0].getValue("_DBL_C1_"));
		assertEquals(new Long(203001), results[1].getValue("_C2_"));
		assertEquals(new Boolean(true), results[1].getValue("_C3_"));
		assertEquals(new Long(404001), results[1].getValue("_C4_"));
		assertEquals(new Long(505001), results[1].getValue("_C5_"));
		assertEquals(new Long(606001), results[1].getValue("_C6_"));
	}
	
	@Test
	public void testGetRowCountSQL(){
		String expected = "SELECT COUNT(ROW_ID) FROM T123";
		String result = SQLUtils.getCountSQL("123");
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetMaxVersionSQL(){
		String expected = "SELECT ROW_VERSION FROM T123S";
		String result = SQLUtils.getStatusMaxVersionSQL("123");
		assertEquals(expected, result);
	}
	
	@Test
	public void testCreateSQLInsertIgnoreFileHandleId(){
		String expected = "INSERT IGNORE INTO T987F (FILE_ID) VALUES(?)";
		String result = SQLUtils.createSQLInsertIgnoreFileHandleId("987");
		assertEquals(expected, result);
	}
	
	@Test
	public void testCreateSQLGetBoundFileHandleId(){
		String expected = "SELECT FILE_ID FROM T987F WHERE FILE_ID IN( :bFIds)";
		String result = SQLUtils.createSQLGetBoundFileHandleId("987");
		assertEquals(expected, result);
	}
	
	@Test
	public void testCreateSQLGetDistinctValues(){
		String expected = "SELECT DISTINCT ROW_BENEFACTORS FROM T123";
		String result = SQLUtils.createSQLGetDistinctValues("syn123", "ROW_BENEFACTORS");
		assertEquals(expected, result);
	}
	
	/**
	 * A helper to create a list of ColumnModels from column model ids.
	 * 
	 * @param values
	 * @return
	 */
	List<ColumnModel> helperCreateColumnsWithIds(String...values){
		List<ColumnModel> list = new LinkedList<ColumnModel>();
		if(values != null){
			for(String value: values){
				ColumnModel cm = new ColumnModel();
				cm.setId(value);
				cm.setColumnType(ColumnType.INTEGER);
				list.add(cm);
			}
		}
		return list;
	}
	
	@Test
	public void testAppendAppendColumnDefinitionWithDefult(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		cm.setDefaultValue("456");
		// call under test
		SQLUtils.appendColumnDefinition(builder, cm);
		assertEquals("_C123_ BIGINT(20) DEFAULT 456 COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendAppendColumnDefinitionDefaultNull(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		cm.setDefaultValue(null);
		// call under test
		SQLUtils.appendColumnDefinition(builder, cm);
		assertEquals("_C123_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	
	@Test
	public void testAppendAddColumn(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		// call under test
		SQLUtils.appendAddColumn(builder, cm, isFirst);
		assertEquals("ADD COLUMN _C123_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendAddColumnNotFirst(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		isFirst = false;
		// call under test
		SQLUtils.appendAddColumn(builder, cm, isFirst);
		assertEquals(", ADD COLUMN _C123_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendAddColumnDouble(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.DOUBLE);
		// call under test
		SQLUtils.appendAddColumn(builder, cm, isFirst);
		assertEquals("ADD COLUMN _C123_ DOUBLE DEFAULT NULL COMMENT 'DOUBLE'"
				+ ", ADD COLUMN _DBL_C123_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test
	public void testAppendAddColumnDoubleNotFirst(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.DOUBLE);
		isFirst = false;
		// call under test
		SQLUtils.appendAddColumn(builder, cm, isFirst);
		assertEquals(", ADD COLUMN _C123_ DOUBLE DEFAULT NULL COMMENT 'DOUBLE'"
				+ ", ADD COLUMN _DBL_C123_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test
	public void testAppendDropColumn(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		// call under test
		SQLUtils.appendDeleteColumn(builder, cm, isFirst);
		assertEquals("DROP COLUMN _C123_", builder.toString());
	}
	
	@Test
	public void testAppendDropColumnNotFirst(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.INTEGER);
		isFirst = false;
		// call under test
		SQLUtils.appendDeleteColumn(builder, cm, isFirst);
		assertEquals(", DROP COLUMN _C123_", builder.toString());
	}
	
	@Test
	public void testAppendDropColumnDouble(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.DOUBLE);
		// call under test
		SQLUtils.appendDeleteColumn(builder, cm, isFirst);
		assertEquals("DROP COLUMN _C123_, DROP COLUMN _DBL_C123_", builder.toString());
	}
	
	@Test
	public void testAppendDropColumnDoubleNotFirst(){
		StringBuilder builder = new StringBuilder();
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(ColumnType.DOUBLE);
		isFirst = false;
		// call under test
		SQLUtils.appendDeleteColumn(builder, cm, isFirst);
		assertEquals(", DROP COLUMN _C123_, DROP COLUMN _DBL_C123_", builder.toString());
	}
	
	@Test
	public void testAppendAddDoubleEnum(){
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.appendAddDoubleEnum(builder, "123");
		assertEquals(", ADD COLUMN _DBL_C123_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test
	public void testAppendDropDoubleEnum(){
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.appendDropDoubleEnum(builder, "123");
		assertEquals(", DROP COLUMN _DBL_C123_", builder.toString());
	}
	
	@Test
	public void testAppendRenameDoubleEnum(){
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.appendRenameDoubleEnum(builder, "123", "456");
		assertEquals(", CHANGE COLUMN _DBL_C123_ _DBL_C456_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testAppendUpdateColumnNullInfo(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = null;
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testAppendUpdateColumnWithNullIndexName(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(true);
		// Index name is required when an index exists.
		oldColumnInfo.setIndexName(null);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
	}
	
	@Test
	public void testAppendUpdateColumnNoIndex(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("CHANGE COLUMN _C123_ _C456_ BOOLEAN DEFAULT NULL COMMENT 'BOOLEAN'", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumnWithIndex(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(true);
		oldColumnInfo.setIndexName("indexName");
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("DROP INDEX indexName, CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumn(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.STRING);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumnNotFirst(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.STRING);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		isFirst = false;
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals(", CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumnOldDouble(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.DOUBLE);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER'"
				+ ", DROP COLUMN _DBL_C123_", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumnNewDouble(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.INTEGER);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.DOUBLE);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("CHANGE COLUMN _C123_ _C456_ DOUBLE DEFAULT NULL COMMENT 'DOUBLE'"
				+ ", ADD COLUMN _DBL_C456_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test
	public void testAppendUpdateColumnOldAndNewDouble(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.DOUBLE);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.DOUBLE);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		SQLUtils.appendUpdateColumn(builder, change, isFirst);
		assertEquals("CHANGE COLUMN _C123_ _C456_ DOUBLE DEFAULT NULL COMMENT 'DOUBLE'"
				+ ", CHANGE COLUMN _DBL_C123_ _DBL_C456_ ENUM ('NaN', 'Infinity', '-Infinity') DEFAULT null", builder.toString());
	}
	
	@Test
	public void testAppendAlterTableSql(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		// call under test
		boolean hasChange = SQLUtils.appendAlterTableSql(builder, change, isFirst);
		assertTrue(hasChange);
		assertEquals("CHANGE COLUMN _C123_ _C456_ BOOLEAN DEFAULT NULL COMMENT 'BOOLEAN'", builder.toString());
	}
	
	@Test
	public void testAppendAlterNoChange(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("123");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		// call under test
		boolean hasChange = SQLUtils.appendAlterTableSql(builder, change, isFirst);
		assertFalse(hasChange);
		assertEquals("", builder.toString());
	}
	
	@Test
	public void testAppendAlterAdd(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = null;
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("123");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		// call under test
		boolean hasChange = SQLUtils.appendAlterTableSql(builder, change, isFirst);
		assertTrue(hasChange);
		assertEquals("ADD COLUMN _C123_ BOOLEAN DEFAULT NULL COMMENT 'BOOLEAN'", builder.toString());
	}
	
	@Test
	public void testAppendAlterDrop(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		// new column
		ColumnModel newColumn = null;
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		// call under test
		boolean hasChange = SQLUtils.appendAlterTableSql(builder, change, isFirst);
		assertTrue(hasChange);
		assertEquals("DROP COLUMN _C123_", builder.toString());
	}
	
	@Test
	public void testAppendAlterOldAndNewNull(){
		StringBuilder builder = new StringBuilder();
		// old column.
		ColumnModel oldColumn = null;
		// new column
		ColumnModel newColumn = null;
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		// call under test
		boolean hasChange = SQLUtils.appendAlterTableSql(builder, change, isFirst);
		assertFalse(hasChange);
		assertEquals("", builder.toString());
	}
	
	@Test
	public void testCreateAlterTableSqlMultiple(){
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		
		oldColumn = new ColumnModel();
		oldColumn.setId("111");
		oldColumn.setColumnType(ColumnType.DOUBLE);
		oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		newColumn = new ColumnModel();
		newColumn.setId("222");
		newColumn.setColumnType(ColumnType.STRING);
		newColumn.setMaximumSize(15L);
		newColumn.setDefaultValue("foo");
		ColumnChangeDetails change2 = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		String tableId = "syn999";
		boolean alterTemp = false;
		// call under test
		String results = SQLUtils.createAlterTableSql(Lists.newArrayList(change, change2), tableId, alterTemp);
		assertEquals("ALTER TABLE T999 "
				+ "CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER', "
				+ "CHANGE COLUMN _C111_ _C222_ VARCHAR(15) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'foo' COMMENT 'STRING', "
				+ "DROP COLUMN _DBL_C111_", results);
	}
	
	@Test
	public void testCreateAlterTableSqlMultipleTempTrue(){
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("456");
		newColumn.setColumnType(ColumnType.INTEGER);
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		
		oldColumn = new ColumnModel();
		oldColumn.setId("111");
		oldColumn.setColumnType(ColumnType.DOUBLE);
		oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		newColumn = new ColumnModel();
		newColumn.setId("222");
		newColumn.setColumnType(ColumnType.STRING);
		newColumn.setMaximumSize(15L);
		newColumn.setDefaultValue("foo");
		ColumnChangeDetails change2 = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		String tableId = "syn999";
		boolean alterTemp = true;
		// call under test
		String results = SQLUtils.createAlterTableSql(Lists.newArrayList(change, change2), tableId, alterTemp);
		assertEquals("ALTER TABLE TEMP999 "
				+ "CHANGE COLUMN _C123_ _C456_ BIGINT(20) DEFAULT NULL COMMENT 'INTEGER', "
				+ "CHANGE COLUMN _C111_ _C222_ VARCHAR(15) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'foo' COMMENT 'STRING', "
				+ "DROP COLUMN _DBL_C111_", results);
	}
	
	@Test
	public void testCreateAlterTableSqlNoChange(){
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.BOOLEAN);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("123");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		boolean alterTemp = false;
		
		String tableId = "syn999";
		// call under test
		String results = SQLUtils.createAlterTableSql(Lists.newArrayList(change), tableId, alterTemp);
		assertEquals("when there are no changes the sql should be null",null, results);
	}
	
	/**
	 * The error from PLFM-4560 was the result of a trailing comma in the SQL
	 * when the second change did not require anything to actually be changed.
	 */
	@Test
	public void testPLFM_4560(){
		// The first is a real change.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.INTEGER);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("123");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		ColumnChangeDetails changeOne = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		 
		// the second should not be a change.
		oldColumn = new ColumnModel();
		oldColumn.setId("444");
		oldColumn.setColumnType(ColumnType.INTEGER);
		// new column
		newColumn = new ColumnModel();
		newColumn.setId("444");
		newColumn.setColumnType(ColumnType.INTEGER);
		ColumnChangeDetails changeTwo = new ColumnChangeDetails(oldColumn, newColumn);
		
		boolean alterTemp = false;
		String tableId = "syn999";
		// call under test
		String results = SQLUtils.createAlterTableSql(Lists.newArrayList(changeOne, changeTwo), tableId, alterTemp);
		assertEquals("ALTER TABLE T999 CHANGE COLUMN _C123_ _C123_ BOOLEAN DEFAULT NULL COMMENT 'BOOLEAN'", results);
	}
	
	/**
	 * The error from PLFM-4560 was the result of a trailing comma in the SQL
	 * when the second change did not require anything to actually be changed.
	 */
	@Test
	public void testPLFM_4560NotFirst(){
		// The first is a real change.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("123");
		oldColumn.setColumnType(ColumnType.INTEGER);
		// new column
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("123");
		newColumn.setColumnType(ColumnType.INTEGER);
		ColumnChangeDetails changeOne = new ColumnChangeDetails(oldColumn, newColumn);
		 
		// the second should not be a change.
		oldColumn = new ColumnModel();
		oldColumn.setId("444");
		oldColumn.setColumnType(ColumnType.INTEGER);
		DatabaseColumnInfo oldColumnInfo = new DatabaseColumnInfo();
		oldColumnInfo.setHasIndex(false);
		// new column
		newColumn = new ColumnModel();
		newColumn.setId("444");
		newColumn.setColumnType(ColumnType.BOOLEAN);
		ColumnChangeDetails changeTwo = new ColumnChangeDetails(oldColumn, oldColumnInfo, newColumn);
		
		boolean alterTemp = false;
		String tableId = "syn999";
		// call under test
		String results = SQLUtils.createAlterTableSql(Lists.newArrayList(changeOne, changeTwo), tableId, alterTemp);
		assertEquals("ALTER TABLE T999 CHANGE COLUMN _C444_ _C444_ BOOLEAN DEFAULT NULL COMMENT 'BOOLEAN'", results);
	}
	
	@Test
	public void testCreateTableIfDoesNotExistSQL(){
		String tableId = "syn123";
		boolean isView = false;
		// call under test
		String sql = SQLUtils.createTableIfDoesNotExistSQL(tableId, isView);
		assertEquals("CREATE TABLE IF NOT EXISTS T123( "
				+ "ROW_ID bigint(20) NOT NULL, "
				+ "ROW_VERSION bigint(20) NOT NULL, "
				+ "PRIMARY KEY (ROW_ID))", sql);
	}
	
	@Test
	public void testCreateTableIfDoesNotExistSQLView(){
		String tableId = "syn123";
		boolean isView = true;
		// call under test
		String sql = SQLUtils.createTableIfDoesNotExistSQL(tableId, isView);
		assertEquals("CREATE TABLE IF NOT EXISTS T123( "
				+ "ROW_ID bigint(20) NOT NULL, "
				+ "ROW_VERSION bigint(20) NOT NULL, "
				+ "ROW_ETAG varchar(36) NOT NULL, "
				+ "ROW_BENEFACTOR bigint(20) NOT NULL, "
				+ "PRIMARY KEY (ROW_ID), "
				+ "KEY `IDX_ETAG` (ROW_ETAG), "
				+ "KEY `IDX_BENEFACTOR` (ROW_BENEFACTOR))", sql);
	}
	
	@Test
	public void testCreateTruncateSql(){
		String sql = SQLUtils.createTruncateSql("syn123");
		assertEquals("TRUNCATE TABLE T123", sql);
	}
	
	@Test
	public void testCreateCardinalitySqlEmpty(){
		String tableId = "syn123";
		List<DatabaseColumnInfo> list = new LinkedList<DatabaseColumnInfo>();
		String results = SQLUtils.createCardinalitySql(list, tableId);
		assertEquals(null, results);
	}
	
	@Test
	public void testCreateCardinalitySqlMultiple(){
		DatabaseColumnInfo one = new DatabaseColumnInfo();
		one.setColumnName("_C111_");
		DatabaseColumnInfo two = new DatabaseColumnInfo();
		two.setColumnName("_C222_");
		
		String tableId = "syn123";
		List<DatabaseColumnInfo> list = Lists.newArrayList(one, two);
		String results = SQLUtils.createCardinalitySql(list, tableId);
		assertEquals("SELECT COUNT(DISTINCT _C111_) AS _C111_, COUNT(DISTINCT _C222_) AS _C222_ FROM T123", results);
	}
	
	public List<DatabaseColumnInfo> createDatabaseColumnInfo(long rowCount){
		List<DatabaseColumnInfo> list = new LinkedList<DatabaseColumnInfo>();
		//row id
		DatabaseColumnInfo rowId = new DatabaseColumnInfo();
		rowId.setColumnName(TableConstants.ROW_ID);
		rowId.setCardinality(rowCount);
		rowId.setHasIndex(true);
		rowId.setIndexName("PRIMARY");
		list.add(rowId);
		//row version
		DatabaseColumnInfo rowVersion = new DatabaseColumnInfo();
		rowVersion.setColumnName(TableConstants.ROW_VERSION);
		rowVersion.setCardinality(1L);
		rowVersion.setHasIndex(true);
		rowVersion.setIndexName("");
		list.add(rowVersion);
		
		// Create rows with descending cardinality.
		for(int i=0; i<rowCount; i++){
			DatabaseColumnInfo info = new DatabaseColumnInfo();
			info.setColumnName("_C"+i+"_");
			info.setCardinality(rowCount-i);
			info.setHasIndex(false);
			info.setIndexName(null);
			list.add(info);
		}
		return list;
	}
	
	@Test
	public void testCalculateIndexChangesIgnoreRowIdAndVersion(){
		String tableId = "syn123";
		int maxNumberOfIndex = 1;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(2);
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);
		assertNotNull(changes);
		assertNotNull(changes.getToAdd());
		assertNotNull(changes.getToRemove());
		assertNotNull(changes.getToRename());
		assertEquals(0, changes.getToAdd().size());
		assertEquals(0, changes.getToRemove().size());
		assertEquals(0, changes.getToRename().size());
	}
	
	@Test
	public void testCalculateIndexChangesUnderMax(){
		String tableId = "syn123";
		int maxNumberOfIndex = 10000;
		int columnCount = 2;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		// call under test
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);
		// both columns should have an index added.
		assertEquals(columnCount, changes.getToAdd().size());
		assertEquals("_C0_", changes.getToAdd().get(0).getColumnName());
		assertEquals("_C1_", changes.getToAdd().get(1).getColumnName());
		
		assertEquals(0, changes.getToRemove().size());
		assertEquals(0, changes.getToRename().size());
	}
	
	@Test
	public void testCalculateIndexChangesUnderMaxNeedsRename(){
		String tableId = "syn123";
		int maxNumberOfIndex = 10000;
		int columnCount = 1;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		// set an index on the last column.
		DatabaseColumnInfo lastInfo = currentInfo.get(2);
		lastInfo.setHasIndex(true);
		// set the wrong name.
		lastInfo.setIndexName("wrongName");
		// call under test.
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);

		assertEquals(0, changes.getToAdd().size());
		assertEquals(0, changes.getToRemove().size());
		// the last column needs to be renamed.
		assertEquals(1, changes.getToRename().size());
		assertEquals("_C0_", changes.getToRename().get(0).getColumnName());
	}
	
	@Test
	public void testCalculateIndexChangesUnderMaxNameCorrect(){
		String tableId = "syn123";
		int maxNumberOfIndex = 10000;
		int columnCount = 1;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		// set an index on the last column.
		DatabaseColumnInfo lastInfo = currentInfo.get(2);
		lastInfo.setHasIndex(true);
		// set the correct name
		lastInfo.setIndexName(SQLUtils.getIndexName(lastInfo.getColumnName()));
		// call under test.
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);

		assertEquals(0, changes.getToAdd().size());		
		assertEquals(0, changes.getToRemove().size());
		// rename not needed.
		assertEquals(0, changes.getToRename().size());
	}
	
	@Test
	public void testCalculateIndexChangesOverMaxWithTooManyIndices(){
		String tableId = "syn123";
		int maxNumberOfIndex = 1;
		int columnCount = 1;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		// set an index on the last column.
		DatabaseColumnInfo lastInfo = currentInfo.get(2);
		lastInfo.setHasIndex(true);
		// set the correct name
		lastInfo.setIndexName(SQLUtils.getIndexName(lastInfo.getColumnName()));
		// call under test.
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);

		assertEquals(0, changes.getToAdd().size());		
		assertEquals(1, changes.getToRemove().size());
		assertEquals("_C0_", changes.getToRemove().get(0).getColumnName());
		assertEquals(0, changes.getToRename().size());
	}
	
	@Test
	public void testCalculateIndexChangesLowCardinalityReplacedWithHigh(){
		String tableId = "syn123";
		int maxNumberOfIndex = 2;
		int columnCount = 2;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		//  index with a lower cardinality
		DatabaseColumnInfo firstInfo = currentInfo.get(2);
		firstInfo.setHasIndex(true);
		firstInfo.setIndexName(SQLUtils.getIndexName(firstInfo.getColumnName()));
		firstInfo.setCardinality(1L);
		
		// no index with a higher cardinality.
		DatabaseColumnInfo lastInfo = currentInfo.get(3);
		lastInfo.setHasIndex(false);
		lastInfo.setCardinality(2L);
		
		// call under test.
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);

		assertEquals(1, changes.getToAdd().size());	
		assertEquals("Higher cardinality should be added","_C1_", changes.getToAdd().get(0).getColumnName());
		assertEquals(1, changes.getToRemove().size());
		assertEquals("Lower cardinality should be dropped.","_C0_", changes.getToRemove().get(0).getColumnName());
		assertEquals(0, changes.getToRename().size());
	}
	
	@Test
	public void testCalculateIndexChangesWithAddRemoveRename(){
		String tableId = "syn123";
		int maxNumberOfIndex = 3;
		int columnCount = 3;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		//  index with a lower cardinality
		DatabaseColumnInfo firstInfo = currentInfo.get(2);
		firstInfo.setHasIndex(true);
		firstInfo.setIndexName(SQLUtils.getIndexName(firstInfo.getColumnName()));
		firstInfo.setCardinality(1L);
		// index with a high cardinality but wrong name.
		DatabaseColumnInfo midInfo = currentInfo.get(3);
		midInfo.setHasIndex(true);
		midInfo.setIndexName("wrongName");
		midInfo.setCardinality(3L);
		
		// no index with a higher cardinality.
		DatabaseColumnInfo lastInfo = currentInfo.get(4);
		lastInfo.setHasIndex(false);
		lastInfo.setCardinality(2L);
		
		// call under test.
		IndexChange changes = SQLUtils.calculateIndexOptimization(currentInfo, tableId, maxNumberOfIndex);

		assertEquals(1, changes.getToAdd().size());	
		assertEquals("Higher cardinality should be added","_C2_", changes.getToAdd().get(0).getColumnName());
		assertEquals(1, changes.getToRemove().size());
		assertEquals("Lower cardinality should be dropped.","_C0_", changes.getToRemove().get(0).getColumnName());
		assertEquals(1, changes.getToRename().size());
		assertEquals("High cardinality should be renamed.","_C1_", changes.getToRename().get(0).getColumnName());
	}
	
	@Test
	public void testCreateAlterSqlEmpty(){
		// test with nothing to do.
		List<DatabaseColumnInfo> toAdd = new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRemove = new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRename = new LinkedList<DatabaseColumnInfo>();
		IndexChange changes = new IndexChange(toAdd, toRemove, toRename);
		String tableId = "syn123";
		// call under test
		String results = SQLUtils.createAlterIndices(changes, tableId);
		assertEquals(null, results);
	}
	
	
	@Test
	public void testCreateAlterSqlAdd(){
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName("_C1_");
		info.setCardinality(1L);
		info.setType(MySqlColumnType.MEDIUMTEXT);
		
		List<DatabaseColumnInfo> toAdd = Lists.newArrayList(info);
		List<DatabaseColumnInfo> toRemove = new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRename = new LinkedList<DatabaseColumnInfo>();
		IndexChange changes = new IndexChange(toAdd, toRemove, toRename);
		String tableId = "syn123";
		// call under test
		String results = SQLUtils.createAlterIndices(changes, tableId);
		assertEquals("ALTER TABLE T123 ADD INDEX _C1_idx_ (_C1_(255))", results);
	}
	
	@Test
	public void testCreateAlterSqlDrop(){
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName("_C1_");
		info.setIndexName("_C1_IDX");
		info.setCardinality(1L);

		List<DatabaseColumnInfo> toAdd = new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRemove = Lists.newArrayList(info);
		List<DatabaseColumnInfo> toRename = new LinkedList<DatabaseColumnInfo>();
		IndexChange changes = new IndexChange(toAdd, toRemove, toRename);
		String tableId = "syn123";
		// call under test
		String results = SQLUtils.createAlterIndices(changes, tableId);
		assertEquals("ALTER TABLE T123 DROP INDEX _C1_IDX", results);
	}
	
	@Test
	public void testCreateAlterSqlRename(){
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName("_C1_");
		info.setIndexName("_C2_idx_");
		info.setType(MySqlColumnType.BIGINT);

		List<DatabaseColumnInfo> toAdd = new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRemove =  new LinkedList<DatabaseColumnInfo>();
		List<DatabaseColumnInfo> toRename = Lists.newArrayList(info);
		IndexChange changes = new IndexChange(toAdd, toRemove, toRename);
		String tableId = "syn123";
		// call under test
		String results = SQLUtils.createAlterIndices(changes, tableId);
		assertEquals("ALTER TABLE T123 DROP INDEX _C2_idx_, ADD INDEX _C1_idx_ (_C1_)", results);
	}
	
	@Test
	public void testAlterSqlAddRemoveRename(){		
		String tableId = "syn123";
		int maxNumberOfIndex = 3;
		int columnCount = 3;
		List<DatabaseColumnInfo> currentInfo = createDatabaseColumnInfo(columnCount);
		//  index with a lower cardinality
		DatabaseColumnInfo firstInfo = currentInfo.get(2);
		firstInfo.setHasIndex(true);
		firstInfo.setCardinality(1L);
		firstInfo.setIndexName("_C0_idx_");
		firstInfo.setType(MySqlColumnType.BIGINT);
		// index with a high cardinality but wrong name.
		DatabaseColumnInfo midInfo = currentInfo.get(3);
		midInfo.setHasIndex(true);
		midInfo.setIndexName("wrongName");
		midInfo.setCardinality(3L);
		midInfo.setType(MySqlColumnType.BIGINT);
		
		// no index with a higher cardinality.
		DatabaseColumnInfo lastInfo = currentInfo.get(4);
		lastInfo.setHasIndex(false);
		lastInfo.setCardinality(2L);
		lastInfo.setType(MySqlColumnType.BIGINT);
		
		String results = SQLUtils.createOptimizedAlterIndices(currentInfo, tableId, maxNumberOfIndex);
		assertEquals("ALTER TABLE T123 "
				+ "DROP INDEX _C0_idx_, "
				+ "DROP INDEX wrongName, ADD INDEX _C1_idx_ (_C1_), "
				+ "ADD INDEX _C2_idx_ (_C2_)", results);
	}
	
	@Test
	public void testCreateReplaceSchemaChange(){
		ColumnModel one = TableModelTestUtils.createColumn(1L);
		ColumnModel two = TableModelTestUtils.createColumn(2L);
		ColumnModel three = TableModelTestUtils.createColumn(3L);
		List<ColumnModel> oldSchema = Lists.newArrayList(one, two);
		List<ColumnModel> newSchema = Lists.newArrayList(two, three);
		// call under test
		List<ColumnChangeDetails> results = SQLUtils.createReplaceSchemaChangeIds(oldSchema, newSchema);
		assertNotNull(results);
		assertEquals(2, results.size());
		// one should be removed
		ColumnChangeDetails toRemove = results.get(0);
		assertEquals("1", toRemove.getOldColumn().getId());
		assertEquals(null, toRemove.getNewColumn());
		// three should be added.
		ColumnChangeDetails toAdd = results.get(1);
		assertEquals(null, toAdd.getOldColumn());
		assertEquals(three, toAdd.getNewColumn());
	}
	
	@Test
	public void testCreateReplaceSchemaChangeNoChange(){
		ColumnModel one = TableModelTestUtils.createColumn(1L);
		ColumnModel two = TableModelTestUtils.createColumn(2L);
		List<ColumnModel> oldSchema = Lists.newArrayList(one, two);
		List<ColumnModel> newSchema = Lists.newArrayList(two, one);
		// call under test
		List<ColumnChangeDetails> results = SQLUtils.createReplaceSchemaChangeIds(oldSchema, newSchema);
		assertNotNull(results);
		assertEquals(0, results.size());
	}
	
	@Test
	public void testGetColumnId(){
		String columnName = SQLUtils.getColumnNameForId("123");
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName(columnName);
		long columnId = SQLUtils.getColumnId(info);
		assertEquals(123L, columnId);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetColumnIdNotAnId(){
		String columnName = SQLUtils.getColumnNameForId("foo");
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName(columnName);
		// call under test.
		SQLUtils.getColumnId(info);
	}
	
	@Test
	public void testGetColumnIds(){
		String columnName = SQLUtils.getColumnNameForId("123");
		DatabaseColumnInfo rowId = new DatabaseColumnInfo();
		rowId.setColumnName(TableConstants.ROW_ID);
		DatabaseColumnInfo rowVersion = new DatabaseColumnInfo();
		rowVersion.setColumnName(TableConstants.ROW_VERSION);
		
		DatabaseColumnInfo info = new DatabaseColumnInfo();
		info.setColumnName(columnName);
		info.setColumnType(ColumnType.STRING);
		info.setMaxSize(22);
		
		DatabaseColumnInfo noType = new DatabaseColumnInfo();
		noType.setColumnName("noType");
		noType.setColumnType(null);
		
		List<DatabaseColumnInfo> infoList = Lists.newArrayList(rowId, rowVersion, info, noType);
		List<ColumnModel> results = SQLUtils.extractSchemaFromInfo(infoList);
		
		ColumnModel cm = new ColumnModel();
		cm.setId("123");
		cm.setColumnType(info.getColumnType());
		cm.setMaximumSize(22L);
		
		List<ColumnModel> expected = Lists.newArrayList(cm);
		assertEquals(expected, results);
	}
	
	@Test 
	public void testTemporaryTableName(){
		String tableId = "syn123";
		String temp = SQLUtils.getTemporaryTableName(tableId);
		assertEquals("TEMP123", temp);
	}
	
	@Test 
	public void testCreateTempTableSql(){
		String tableId = "syn123";
		String sql = SQLUtils.createTempTableSql(tableId);
		assertEquals("CREATE TABLE TEMP123 LIKE T123", sql);
	}
	
	@Test 
	public void testCopyTableToTempSql(){
		String tableId = "syn123";
		String sql = SQLUtils.copyTableToTempSql(tableId);
		assertEquals("INSERT INTO TEMP123 SELECT * FROM T123 ORDER BY ROW_ID", sql);
	}
	
	@Test 
	public void testDeleteTempTableSql(){
		String tableId = "syn123";
		String sql = SQLUtils.deleteTempTableSql(tableId);
		assertEquals("DROP TABLE IF EXISTS TEMP123", sql);
	}

	
	@Test
	public void testTranslateColumnsEntityField(){
		ColumnModel cm = EntityField.benefactorId.getColumnModel();
		cm.setId("123");
		int index = 4;
		// call under test
		ColumnMetadata meta = SQLUtils.translateColumns(cm, index);
		assertEquals(cm, meta.getColumnModel());
		assertEquals(index, meta.getColumnIndex());
		assertEquals("_C123_", meta.getColumnNameForId());
		assertEquals(EntityField.benefactorId, meta.getEntityField());
		assertEquals(TableConstants.ENTITY_REPLICATION_ALIAS, meta.getTableAlias());
		assertEquals(EntityField.benefactorId.getDatabaseColumnName(), meta.getSelectColumnName());
		assertEquals(null, meta.getAnnotationType());
	}
	
	@Test
	public void testTranslateColumnsAnnotation(){
		ColumnModel cm = new ColumnModel();
		cm.setName("foo");
		cm.setColumnType(ColumnType.STRING);
		cm.setMaximumSize(50L);
		cm.setId("123");
		int index = 4;
		// call under test.
		ColumnMetadata meta = SQLUtils.translateColumns(cm, index);
		assertEquals(cm, meta.getColumnModel());
		assertEquals(index, meta.getColumnIndex());
		assertEquals("_C123_", meta.getColumnNameForId());
		assertEquals(null, meta.getEntityField());
		assertEquals("A4", meta.getTableAlias());
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_STRING_VALUE, meta.getSelectColumnName());
		assertEquals(AnnotationType.STRING, meta.getAnnotationType());
	}
	
	@Test
	public void testTranslateColumnTypeToAnnotationType(){
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.STRING));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.BOOLEAN));
		assertEquals(AnnotationType.DATE, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.DATE));
		assertEquals(AnnotationType.DOUBLE, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.DOUBLE));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.ENTITYID));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.FILEHANDLEID));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.USERID));
		assertEquals(AnnotationType.LONG, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.INTEGER));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.LARGETEXT));
		assertEquals(AnnotationType.STRING, SQLUtils.translateColumnTypeToAnnotationType(ColumnType.LINK));
	}
	
	@Test
	public void testTranslateColumnTypeToAnnotationValueName(){
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_STRING_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.STRING));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_BOOLEAN_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.BOOLEAN));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_LONG_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.DATE));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_DOUBLE_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.DOUBLE));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_LONG_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.ENTITYID));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_LONG_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.FILEHANDLEID));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_LONG_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.USERID));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_LONG_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.INTEGER));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_STRING_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.LARGETEXT));
		assertEquals(TableConstants.ANNOTATION_REPLICATION_COL_STRING_VALUE, SQLUtils.translateColumnTypeToAnnotationValueName(ColumnType.LINK));
	}
	
	@Test
	public void testbuildInsertValues(){
		ColumnModel one = EntityField.benefactorId.getColumnModel();
		one.setId("1");
		ColumnModel two = TableModelTestUtils.createColumn(2L);
		ColumnModel three = new ColumnModel();
		three.setId("3");
		three.setColumnType(ColumnType.DOUBLE);
		three.setName("three");
		List<ColumnModel> schema = Lists.newArrayList(one, two, three);
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(schema);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildInsertValues(builder, metaList);
		assertEquals("ROW_ID, ROW_VERSION, ROW_ETAG, ROW_BENEFACTOR, _C1_, _C2_, _DBL_C3_, _C3_", builder.toString());
	}
	
	@Test
	public void testBuildSelectEachColumnType(){
		// Build a select for each type.
		List<ColumnModel> allTypes = new LinkedList<>();
		int i = 0;
		for(ColumnType type: ColumnType.values()){
			ColumnModel cm = new ColumnModel();
			cm.setName(type.name().toLowerCase());
			cm.setColumnType(type);
			cm.setId(""+i);
			allTypes.add(cm);
			i++;
		}
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(allTypes);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildSelect(builder, metaList);
		assertEquals(
				"R.ID, R.CURRENT_VERSION, R.ETAG, R.BENEFACTOR_ID"
				+ ", A0.STRING_VALUE AS _C0_"
				+ ", A1.DOUBLE_ABSTRACT AS _DBL_C1_"
				+ ", A1.DOUBLE_VALUE AS _C1_"
				+ ", A2.LONG_VALUE AS _C2_"
				+ ", A3.BOOLEAN_VALUE AS _C3_"
				+ ", A4.LONG_VALUE AS _C4_"
				+ ", A5.LONG_VALUE AS _C5_"
				+ ", A6.LONG_VALUE AS _C6_"
				+ ", A7.STRING_VALUE AS _C7_"
				+ ", A8.STRING_VALUE AS _C8_"
				+ ", A9.LONG_VALUE AS _C9_"
				, builder.toString());
	}
	
	@Test
	public void testBuildSelectEachEntityType(){
		// Build a select for each type.
		List<ColumnModel> allTypes = new LinkedList<>();
		int i = 0;
		for(EntityField field: EntityField.values()){
			ColumnModel cm = field.getColumnModel();
			cm.setId(""+i);
			allTypes.add(cm);
			i++;
		}
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(allTypes);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildSelect(builder, metaList);
		assertEquals(
				"R.ID"
				+ ", R.CURRENT_VERSION"
				+ ", R.ETAG"
				+ ", R.BENEFACTOR_ID"
				+ ", R.ID AS _C0_"
				+ ", R.NAME AS _C1_"
				+ ", R.CREATED_ON AS _C2_"
				+ ", R.CREATED_BY AS _C3_"
				+ ", R.ETAG AS _C4_"
				+ ", R.TYPE AS _C5_"
				+ ", R.CURRENT_VERSION AS _C6_"
				+ ", R.PARENT_ID AS _C7_"
				+ ", R.BENEFACTOR_ID AS _C8_"
				+ ", R.PROJECT_ID AS _C9_"
				+ ", R.MODIFIED_ON AS _C10_"
				+ ", R.MODIFIED_BY AS _C11_"
				+ ", R.FILE_ID AS _C12_"
				, builder.toString());
	}
	
	@Test
	public void testBuildJoinsNoAnnotations(){
		ColumnModel one = EntityField.benefactorId.getColumnModel();
		one.setId("1");
		ColumnModel two = EntityField.id.getColumnModel();
		two.setId("2");
		List<ColumnModel> schema = Lists.newArrayList(one, two);
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(schema);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildJoins(metaList, builder);
		assertEquals("", builder.toString());
	}
	
	@Test
	public void testBuildJoinsOneAnnotation(){
		ColumnModel one = EntityField.benefactorId.getColumnModel();
		one.setId("1");
		ColumnModel two = TableModelTestUtils.createColumn(2L);
		List<ColumnModel> schema = Lists.newArrayList(one, two);
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(schema);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildJoins(metaList, builder);
		assertEquals(" LEFT OUTER JOIN ANNOTATION_REPLICATION A1 ON"
				+ " (R.ID = A1.ENTITY_ID AND A1.ANNO_KEY = 'col_2')", builder.toString());
	}
	
	@Test
	public void testBuildJoinsMultipleAnnotation(){
		ColumnModel one =  TableModelTestUtils.createColumn(1L);
		ColumnModel two = TableModelTestUtils.createColumn(2L);
		two.setColumnType(ColumnType.INTEGER);
		List<ColumnModel> schema = Lists.newArrayList(one, two);
		List<ColumnMetadata> metaList = SQLUtils.translateColumns(schema);
		StringBuilder builder = new StringBuilder();
		// call under test
		SQLUtils.buildJoins(metaList, builder);
		assertEquals(" LEFT OUTER JOIN ANNOTATION_REPLICATION A0 ON"
				+ " (R.ID = A0.ENTITY_ID AND A0.ANNO_KEY = 'col_1')"
				+ " LEFT OUTER JOIN ANNOTATION_REPLICATION A1 ON "
				+ "(R.ID = A1.ENTITY_ID AND A1.ANNO_KEY = 'col_2')", builder.toString());
	}
	
	@Test
	public void testCreateViewTypeFilterFile(){
		String result = SQLUtils.createViewTypeFilter(ViewType.file);
		assertEquals("TYPE IN ('file')", result);
	}
	
	@Test
	public void testCreateViewTypeFilterProject(){
		String result = SQLUtils.createViewTypeFilter(ViewType.project);
		assertEquals("TYPE IN ('project')", result);
	}
	
	@Test
	public void testCreateViewTypeFilterFileAndTable(){
		String result = SQLUtils.createViewTypeFilter(ViewType.file_and_table);
		assertEquals("TYPE IN ('file','table')", result);
	}
	
	@Test
	public void testCreateSelectInsertFromEntityReplication(){
		String viewId = "syn123";
		ColumnModel one = TableModelTestUtils.createColumn(1L);
		ColumnModel id = EntityField.id.getColumnModel();
		id.setId("2");
		List<ColumnModel> schema = Lists.newArrayList(one, id);
		ViewType type = ViewType.file;
		String sql = SQLUtils.createSelectInsertFromEntityReplication(viewId, type, schema);
		assertEquals("INSERT INTO T123(ROW_ID, ROW_VERSION, ROW_ETAG, ROW_BENEFACTOR, _C1_, _C2_)"
				+ " SELECT R.ID, R.CURRENT_VERSION, R.ETAG, R.BENEFACTOR_ID, A0.STRING_VALUE AS _C1_, R.ID AS _C2_"
				+ " FROM ENTITY_REPLICATION R"
				+ " LEFT OUTER JOIN ANNOTATION_REPLICATION A0"
				+ " ON (R.ID = A0.ENTITY_ID AND A0.ANNO_KEY = 'col_1')"
				+ " WHERE R.PARENT_ID IN (:parentIds) AND TYPE IN ('file')", sql);
	}
	
	@Test
	public void testCreateSelectInsertFromEntityReplicationProjectView(){
		String viewId = "syn123";
		ColumnModel one = TableModelTestUtils.createColumn(1L);
		ColumnModel id = EntityField.id.getColumnModel();
		id.setId("2");
		List<ColumnModel> schema = Lists.newArrayList(one, id);
		ViewType type = ViewType.project;
		String sql = SQLUtils.createSelectInsertFromEntityReplication(viewId, type, schema);
		assertEquals("INSERT INTO T123(ROW_ID, ROW_VERSION, ROW_ETAG, ROW_BENEFACTOR, _C1_, _C2_)"
				+ " SELECT R.ID, R.CURRENT_VERSION, R.ETAG, R.BENEFACTOR_ID, A0.STRING_VALUE AS _C1_, R.ID AS _C2_"
				+ " FROM ENTITY_REPLICATION R"
				+ " LEFT OUTER JOIN ANNOTATION_REPLICATION A0"
				+ " ON (R.ID = A0.ENTITY_ID AND A0.ANNO_KEY = 'col_1')"
				+ " WHERE R.ID IN (:parentIds) AND TYPE IN ('project')", sql);
	}

	@Test
	public void testCreateSelectInsertFromEntityReplicationWithDouble(){
		String viewId = "syn123";
		ColumnModel doubleAnnotation = new ColumnModel();
		doubleAnnotation.setColumnType(ColumnType.DOUBLE);
		doubleAnnotation.setId("3");
		doubleAnnotation.setName("doubleAnnotation");
		List<ColumnModel> schema = Lists.newArrayList(doubleAnnotation);
		ViewType type = ViewType.file;
		String sql = SQLUtils.createSelectInsertFromEntityReplication(viewId, type, schema);
		assertEquals("INSERT INTO T123(ROW_ID, ROW_VERSION, ROW_ETAG, ROW_BENEFACTOR, _DBL_C3_, _C3_)"
				+ " SELECT R.ID, R.CURRENT_VERSION, R.ETAG, R.BENEFACTOR_ID,"
				+ " A0.DOUBLE_ABSTRACT AS _DBL_C3_, A0.DOUBLE_VALUE AS _C3_"
				+ " FROM ENTITY_REPLICATION R"
				+ " LEFT OUTER JOIN ANNOTATION_REPLICATION A0"
				+ " ON (R.ID = A0.ENTITY_ID AND A0.ANNO_KEY = 'doubleAnnotation')"
				+ " WHERE R.PARENT_ID IN (:parentIds) AND TYPE IN ('file')", sql);
	}
	
	@Test
	public void testBuildTableViewCRC32Sql(){
		String viewId = "syn123";
		String sql = SQLUtils.buildTableViewCRC32Sql(viewId);
		assertEquals("SELECT SUM(CRC32(CONCAT(ROW_ID, '-', ROW_ETAG, '-', ROW_BENEFACTOR))) FROM T123", sql);
	}
	
	@Test
	public void testGetCalculateCRC32SqlProject(){
		String sql = SQLUtils.getCalculateCRC32Sql(ViewType.project);
		String expected = 
				"SELECT SUM(CRC32(CONCAT(ID, '-',ETAG, '-', BENEFACTOR_ID)))"
				+ " FROM ENTITY_REPLICATION WHERE TYPE IN ('project') AND ID IN (:parentIds)";
		assertEquals(expected, sql);
	}
	
	@Test
	public void testGetCalculateCRC32SqlFile(){
		String sql = SQLUtils.getCalculateCRC32Sql(ViewType.file);
		String expected = 
				"SELECT SUM(CRC32(CONCAT(ID, '-',ETAG, '-', BENEFACTOR_ID)))"
				+ " FROM ENTITY_REPLICATION WHERE TYPE IN ('file') AND PARENT_ID IN (:parentIds)";
		assertEquals(expected, sql);
	}
	
	@Test
	public void testGetCalculateCRC32SqlFileAndTable(){
		String sql = SQLUtils.getCalculateCRC32Sql(ViewType.file_and_table);
		String expected = 
				"SELECT SUM(CRC32(CONCAT(ID, '-',ETAG, '-', BENEFACTOR_ID)))"
				+ " FROM ENTITY_REPLICATION WHERE TYPE IN ('file','table') AND PARENT_ID IN (:parentIds)";
		assertEquals(expected, sql);
	}
	
	@Test
	public void testBuildSelectRowIds(){
		RowReference ref1 = new RowReference();
		ref1.setRowId(222L);
		RowReference ref2 = new RowReference();
		ref2.setRowId(333L);
		
		ColumnModel c1 = TableModelTestUtils.createColumn(1L);
		ColumnModel c2 = TableModelTestUtils.createColumn(2L);
		
		String sql = SQLUtils.buildSelectRowIds("syn123", Lists.newArrayList(ref1, ref2), Lists.newArrayList(c1,  c2));
		String expected = "SELECT 'col_1', 'col_2' FROM syn123 WHERE ROW_ID IN (222, 333)";
		assertEquals(expected, sql);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testBuildSelectRowIdsNullRefts(){
		RowReference ref1 = new RowReference();
		ref1.setRowId(222L);
		RowReference ref2 = new RowReference();
		ref2.setRowId(333L);
		
		ColumnModel c1 = TableModelTestUtils.createColumn(1L);
		ColumnModel c2 = TableModelTestUtils.createColumn(2L);
		
		SQLUtils.buildSelectRowIds("syn123", null, Lists.newArrayList(c1,  c2));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testBuildSelectRowIdsNullColumns(){
		RowReference ref1 = new RowReference();
		ref1.setRowId(222L);
		RowReference ref2 = new RowReference();
		ref2.setRowId(333L);
		SQLUtils.buildSelectRowIds("syn123", Lists.newArrayList(ref1, ref2), null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testBuildSelectRowIdsEmptyRefs(){
		ColumnModel c1 = TableModelTestUtils.createColumn(1L);
		ColumnModel c2 = TableModelTestUtils.createColumn(2L);
		
		String sql = SQLUtils.buildSelectRowIds("syn123", new LinkedList<RowReference>(), Lists.newArrayList(c1,  c2));
		String expected = "SELECT col_1, col_2 FROM syn123 WHERE ROW_ID IN (222, 333)";
		assertEquals(expected, sql);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testBuildSelectRowIdsEmptyColumns(){
		RowReference ref1 = new RowReference();
		ref1.setRowId(222L);
		RowReference ref2 = new RowReference();
		ref2.setRowId(333L);
		SQLUtils.buildSelectRowIds("syn123", Lists.newArrayList(ref1, ref2), new LinkedList<ColumnModel>());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testBuildSelectRowIdsNullRefRowId(){
		RowReference ref1 = new RowReference();
		ref1.setRowId(null);
		RowReference ref2 = new RowReference();
		ref2.setRowId(333L);
		
		ColumnModel c1 = TableModelTestUtils.createColumn(1L);
		ColumnModel c2 = TableModelTestUtils.createColumn(2L);
		
		SQLUtils.buildSelectRowIds("syn123", Lists.newArrayList(ref1, ref2), Lists.newArrayList(c1,  c2));
	}
	
	@Test
	public void testMatchChangesToCurrentInfoOldExists(){
		DatabaseColumnInfo rowId = new DatabaseColumnInfo();
		rowId.setColumnName(TableConstants.ROW_ID);
		DatabaseColumnInfo one = new DatabaseColumnInfo();
		one.setColumnName("_C111_");
		one.setColumnType(ColumnType.STRING);
		DatabaseColumnInfo two = new DatabaseColumnInfo();
		two.setColumnName("_C222_");
		two.setColumnType(ColumnType.STRING);
		List<DatabaseColumnInfo> curretIndexSchema = Lists.newArrayList(rowId, one, two);
		// the old exists in the current.
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("222");
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("333");
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		
		List<ColumnChangeDetails> changes = Lists.newArrayList(change);
		
		// call under test
		List<ColumnChangeDetails> results = SQLUtils.matchChangesToCurrentInfo(curretIndexSchema, changes);
		// the results should be unchanged.
		assertEquals(changes, results);
		ColumnChangeDetails updated = results.get(0);
		// should not be the same instance.
		assertFalse(change == updated);
	}
	
	@Test
	public void testMatchChangesToCurrentInfoOldDoesNotExist(){
		DatabaseColumnInfo rowId = new DatabaseColumnInfo();
		rowId.setColumnName(TableConstants.ROW_ID);
		DatabaseColumnInfo one = new DatabaseColumnInfo();
		one.setColumnName("_C111_");
		one.setColumnType(ColumnType.STRING);
		DatabaseColumnInfo two = new DatabaseColumnInfo();
		two.setColumnName("_C222_");
		two.setColumnType(ColumnType.STRING);
		List<DatabaseColumnInfo> curretIndexSchema = Lists.newArrayList(rowId, one, two);
		
		// the old does not exist in the current
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("333");
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("444");
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		
		List<ColumnChangeDetails> changes = Lists.newArrayList(change);
		
		// call under test
		List<ColumnChangeDetails> results = SQLUtils.matchChangesToCurrentInfo(curretIndexSchema, changes);
		// the results should be changed
		assertNotNull(results);
		assertEquals(1, results.size());
		ColumnChangeDetails updated = results.get(0);
		// should not be the same instance.
		assertFalse(change == updated);
		assertEquals(null, updated.getOldColumn());
		assertEquals(newColumn, updated.getNewColumn());
	}
	
	@Test
	public void testMatchChangesToCurrentInfoCurrentEmpty(){
		List<DatabaseColumnInfo> curretIndexSchema = new LinkedList<>();
		// the old does not exist in the current
		ColumnModel oldColumn = new ColumnModel();
		oldColumn.setId("333");
		ColumnModel newColumn = new ColumnModel();
		newColumn.setId("444");
		ColumnChangeDetails change = new ColumnChangeDetails(oldColumn, newColumn);
		
		List<ColumnChangeDetails> changes = Lists.newArrayList(change);
		
		// call under test
		List<ColumnChangeDetails> results = SQLUtils.matchChangesToCurrentInfo(curretIndexSchema, changes);
		// the results should be changed
		assertNotNull(results);
		assertEquals(1, results.size());
		ColumnChangeDetails updated = results.get(0);
		// should not be the same instance.
		assertFalse(change == updated);
		assertEquals(null, updated.getOldColumn());
		assertEquals(newColumn, updated.getNewColumn());
	}
	
	/**
	 * This is a test case for PLFM-4235 where a view's string columns were
	 * set to be too small for the annotation values.
	 */
	@Test
	public void testDetermineCauseOfExceptionTooSmall() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("foo");
		annotationModel.setColumnType(ColumnType.STRING);
		annotationModel.setMaximumSize(11L);
		try {
			// call under test
			SQLUtils.determineCauseOfException(oringal, columnModel,
					annotationModel);
			fail("Should have failed.");
		} catch (IllegalArgumentException expected) {
			assertEquals(
					"The size of the column 'foo' is too small.  The column size needs to be at least 11 characters.",
					expected.getMessage());
			// the cause should be kept
			assertEquals(oringal, expected.getCause());
		}
	}
	
	@Test
	public void testDetermineCauseOfExceptionSameSize() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("foo");
		annotationModel.setColumnType(ColumnType.STRING);
		annotationModel.setMaximumSize(10L);
		// call under test
		SQLUtils.determineCauseOfException(oringal, columnModel, annotationModel);
	}
	
	@Test
	public void testDetermineCauseOfExceptionNameDoesNotMatch() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("bar");
		annotationModel.setColumnType(ColumnType.STRING);
		annotationModel.setMaximumSize(11L);
		// call under test
		SQLUtils.determineCauseOfException(oringal, columnModel, annotationModel);
	}
	
	@Test
	public void testDetermineCauseOfExceptionTypeDoesNotMatch() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("foo");
		annotationModel.setColumnType(ColumnType.INTEGER);
		annotationModel.setMaximumSize(11L);
		// call under test
		SQLUtils.determineCauseOfException(oringal, columnModel, annotationModel);
	}
	
	
	/**
	 * This is a test for PLFM-4260 where a boolean column was mapped to a string annotation.
	 */
	@Test
	public void testDetermineCauseOfExceptionWrongType() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.BOOLEAN);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("foo");
		annotationModel.setColumnType(ColumnType.STRING);
		annotationModel.setMaximumSize(11L);
		try {
			// call under test
			SQLUtils.determineCauseOfException(oringal, columnModel,
					annotationModel);
			fail("Should have failed.");
		} catch (IllegalArgumentException expected) {
			assertEquals(
					"Cannot insert an annotation value of type STRING into column 'foo' which is of type BOOLEAN.",
					expected.getMessage());
			// the cause should be kept
			assertEquals(oringal, expected.getCause());
		}
	}
	
	@Test
	public void testDetermineCauseOfExceptionLists() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);

		ColumnModel annotationModel = new ColumnModel();
		annotationModel.setName("foo");
		annotationModel.setColumnType(ColumnType.STRING);
		annotationModel.setMaximumSize(11L);
		try {
			// call under test
			SQLUtils.determineCauseOfException(oringal, Lists.newArrayList(columnModel),
					Lists.newArrayList(annotationModel));
			fail("Should have failed.");
		} catch (IllegalArgumentException expected) {
			// the cause should be kept
			assertEquals(oringal, expected.getCause());
		}
	}
	
	@Test
	public void testDetermineCauseOfExceptionListsMultipleValues() {
		Exception oringal = new Exception("Some exception");
		ColumnModel columnModel = new ColumnModel();
		columnModel.setName("foo");
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(10L);
		// type does not match.
		ColumnModel a1 = new ColumnModel();
		a1.setName("foo");
		a1.setColumnType(ColumnType.INTEGER);

		ColumnModel a2 = new ColumnModel();
		a2.setName("foo");
		a2.setColumnType(ColumnType.STRING);
		a2.setMaximumSize(11L);
		
		try {
			// call under test
			SQLUtils.determineCauseOfException(oringal, Lists.newArrayList(columnModel),
					Lists.newArrayList(a1, a2));
			fail("Should have failed.");
		} catch (IllegalArgumentException expected) {
			// the cause should be kept
			assertEquals(oringal, expected.getCause());
		}
	}
	
	@Test
	public void testGetViewScopeFilterColumnForType() {
		assertEquals(TableConstants.ENTITY_REPLICATION_COL_ID,
				SQLUtils.getViewScopeFilterColumnForType(ViewType.project));
		assertEquals(TableConstants.ENTITY_REPLICATION_COL_PARENT_ID,
				SQLUtils.getViewScopeFilterColumnForType(ViewType.file));
		assertEquals(TableConstants.ENTITY_REPLICATION_COL_PARENT_ID,
				SQLUtils.getViewScopeFilterColumnForType(ViewType.file_and_table));
	}
	
	@Test
	public void testGetDistinctAnnotationColumnsSqlFileView(){
		String sql = SQLUtils.getDistinctAnnotationColumnsSql(ViewType.file);
		String expected = TableConstants.ENTITY_REPLICATION_COL_PARENT_ID+" IN (:parentIds)";
		assertTrue(sql.contains(expected));
	}
	
	@Test
	public void testGetDistinctAnnotationColumnsSqlProjectView(){
		String sql = SQLUtils.getDistinctAnnotationColumnsSql(ViewType.project);
		String expected = TableConstants.ENTITY_REPLICATION_COL_ID+" IN (:parentIds)";
		assertTrue(sql.contains(expected));
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementString() throws SQLException{
		// string value
		annotationDto.setValue("someString");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		verify(mockPreparedStatement).setLong(1, annotationDto.getEntityId());
		verify(mockPreparedStatement).setString(2, annotationDto.getKey());
		verify(mockPreparedStatement).setString(3, annotationDto.getType().name());
		verify(mockPreparedStatement).setString(4, annotationDto.getValue());
		// all others should be set to null since the string cannot be converted to any other type.
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementBooleanTrue() throws SQLException{
		// string value
		annotationDto.setValue("True");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// type can be set as a boolean.
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setBoolean(8, Boolean.TRUE);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementBooleanFalse() throws SQLException{
		// string value
		annotationDto.setValue("false");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// type can be set as a boolean.
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setBoolean(8, Boolean.FALSE);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementSynapseId() throws SQLException{
		// string value
		annotationDto.setValue("syn123456");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// the synapse ID can be set as a long.
		verify(mockPreparedStatement).setLong(5, 123456L);
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementDateString() throws SQLException{
		// string value
		annotationDto.setValue("1970-1-1 00:00:00.123");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// the date string can be treated as a long.
		verify(mockPreparedStatement).setLong(5, 123L);
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementLong() throws SQLException{
		// string value
		annotationDto.setValue("123");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// can be a long or a double
		verify(mockPreparedStatement).setLong(5, 123L);
		verify(mockPreparedStatement).setDouble(6, 123);
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementFiniteDouble() throws SQLException{
		// string value
		annotationDto.setValue("123.456");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		// value can be a double
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setDouble(6, 123.456);
		// 7 is the abstract enum for doubles.  Null since this is a finite value
		verify(mockPreparedStatement).setNull(7, Types.VARCHAR);
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementDoubleNaN() throws SQLException{
		// string value
		annotationDto.setValue("NAN");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		// the approximation of NaN is null.
		verify(mockPreparedStatement).setNull(6, Types.DOUBLE);
		// 7 is the abstract enum for doubles.  Null since this is a finite value
		verify(mockPreparedStatement).setString(7, AbstractDouble.NAN.getEnumerationValue());
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementInfinity() throws SQLException{
		// string value
		annotationDto.setValue("+Infinity");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setDouble(6, AbstractDouble.POSITIVE_INFINITY.getApproximateValue());
		verify(mockPreparedStatement).setString(7, AbstractDouble.POSITIVE_INFINITY.getEnumerationValue());
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
	
	@Test
	public void testWriteAnnotationDtoToPreparedStatementNegativeInfinity() throws SQLException{
		// string value
		annotationDto.setValue("-Infinity");
		// Call under test
		SQLUtils.writeAnnotationDtoToPreparedStatement(mockPreparedStatement, annotationDto);
		verify(mockPreparedStatement).setNull(5, Types.BIGINT);
		verify(mockPreparedStatement).setDouble(6, AbstractDouble.NEGATIVE_INFINITY.getApproximateValue());
		verify(mockPreparedStatement).setString(7, AbstractDouble.NEGATIVE_INFINITY.getEnumerationValue());
		verify(mockPreparedStatement).setNull(8, Types.BOOLEAN);
	}
}
