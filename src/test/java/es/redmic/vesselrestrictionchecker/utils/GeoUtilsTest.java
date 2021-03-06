package es.redmic.vesselrestrictionchecker.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;

public class GeoUtilsTest {

	@Test
	public void getGeoHashFromPoint_returnStringCode_whenPointIsValid() {

		String codeExpected = "etj";

		String result = GeoUtils.getGeoHash(28.554224326054886, -14.75341796875, 3);

		assertEquals(codeExpected, result);
	}

	@Test(expected = InvalidShapeException.class)
	public void getGeoHashFromPoint_throwException_whenPointIsNotValid() {

		GeoUtils.getGeoHash(91.554224326054886, 190.75341796875, 3);
	}

	@Test(expected = AssertionError.class)
	public void getGeoHashFromPoint_throwException_whenPrecisionIsGreaterThanMaxLevels()
			throws InvalidShapeException, ParseException {

		GeoUtils.getGeoHash(91.554224326054886, 190.75341796875, 6);
	}

	@Test
	public void getGeoHashFromShape_returnStringCodeList_whenshapeIsValid()
			throws InvalidShapeException, ParseException {

		// @formatter:off

		String geometry_wkt = "POLYGON((-11.0400390625 30.844326170562077,"
				+ "-20.07080078125 30.731069927308248,-20.048828125 25.99616671236511,"
				+ "-14.2041015625 26.547845929365057,-12.68798828125 28.438360609130864,"
				+ "-11.0400390625 30.844326170562077))";

		List<String> codeListExpected = new ArrayList<String>(
				Arrays.asList("es9", "esc", "esd", "ese", "esf", "esg", "ess", "est", "esu", "esv", "esy", "et1", "et3",
						"et4", "et5", "et6", "et7", "eth", "etj", "etk", "etm", "etn", "etp", "etq", "etr", "ev2"));

		// @formatter:on

		List<String> result = GeoUtils.getGeoHash(geometry_wkt, 3);

		assertEquals(codeListExpected, result);
	}

	@Test(expected = InvalidShapeException.class)
	public void getGeoHashFromShape_throwException_whenShapeIsNotValid() throws InvalidShapeException, ParseException {

		GeoUtils.getGeoHash("POINT(190.75341796875 91.554224326054886)", 3);
	}

	@Test(expected = AssertionError.class)
	public void getGeoHashFromShape_throwException_whenPrecisionIsGreaterThanMaxLevels()
			throws InvalidShapeException, ParseException {

		GeoUtils.getGeoHash("POINT(190.75341796875 91.554224326054886)", 6);
	}

	@Test
	public void getGeoHashFromPointAndgetGeoHashFromShape_returnEqualStringCode_whenPointAndShapeAreEquals()
			throws InvalidShapeException, ParseException {

		String geoHashFromPoint = GeoUtils.getGeoHash(28.554224326054886, -14.75341796875, 3);

		List<String> geoHashFromShape = GeoUtils.getGeoHash("POINT(-14.75341796875 28.554224326054886)", 3);

		assertEquals(1, geoHashFromShape.size());
		assertEquals(geoHashFromPoint, geoHashFromShape.get(0));
	}

	@Test
	public void shapeContainsGeometry_ReturnTrue_IfAreaContainsPoint() throws InvalidShapeException, ParseException {

		Shape pointShape = GeoUtils.getShapeFromWKT("POINT(-16.89305824790779 28.123415162762214)");

		Shape areaShape = GeoUtils
				.getShapeFromWKT("POLYGON((-17.115923627143275 28.26107051182232,-16.86186478925265 28.268327827045535,"
						+ " -16.7053096134714 27.970373554893733,-17.047259076362025 27.974012125154626,"
						+ " -17.115923627143275 28.26107051182232))");

		assertTrue(GeoUtils.shapeContainsGeometry(areaShape, pointShape));
	}

	@Test
	public void shapeContainsGeometry_ReturnFalse_IfAreaNoContainsPoint() throws InvalidShapeException, ParseException {

		Shape pointShape = GeoUtils.getShapeFromWKT("POINT(-17.89305824790779 28.123415162762214)");

		Shape areaShape = GeoUtils
				.getShapeFromWKT("POLYGON((-17.115923627143275 28.26107051182232,-16.86186478925265 28.268327827045535,"
						+ " -16.7053096134714 27.970373554893733,-17.047259076362025 27.974012125154626,"
						+ " -17.115923627143275 28.26107051182232))");

		assertFalse(GeoUtils.shapeContainsGeometry(areaShape, pointShape));
	}

	@Test
	public void shapeContainsGeometry_ReturnTrue_IfAreaAndPointIntersect()
			throws InvalidShapeException, ParseException {

		Shape pointShape = GeoUtils.getShapeFromWKT("POINT(-17.115923627143275 28.26107051182232)");

		Shape areaShape = GeoUtils
				.getShapeFromWKT("POLYGON((-17.115923627143275 28.26107051182232,-16.86186478925265 28.268327827045535,"
						+ " -16.7053096134714 27.970373554893733,-17.047259076362025 27.974012125154626,"
						+ " -17.115923627143275 28.26107051182232))");

		assertTrue(GeoUtils.shapeContainsGeometry(areaShape, pointShape));
	}
}
