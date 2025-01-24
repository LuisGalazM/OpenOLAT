/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.resource.accesscontrol;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.manager.ACOfferToOrganisationDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.resource.accesscontrol.model.TokenAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 * Description:<br>
 *
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACOfferManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACOfferDAO acOfferManager;
	@Autowired
	private ACService acService;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private ACOfferToOrganisationDAO offerToOrganisationDao;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acService);
	}

	@Test
	public void testSaveOffer() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestSaveOffer");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		if(offer instanceof OfferImpl) {
			OfferImpl offerImpl = (OfferImpl)offer;
			offerImpl.setToken("token1");
		}
		offer.setValidFrom(new Date());
		offer.setValidTo(new Date());
		BigDecimal cancellingFeeAmount = new BigDecimal("10.00");
		Price cancellingFee = new PriceImpl(cancellingFeeAmount, "USD");
		offer.setCancellingFee(cancellingFee);
		offer.setCancellingFeeDeadlineDays(10);
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//check if the offer is saved
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null, false, null, null);
		assertNotNull(offers);
		assertEquals(1, offers.size());
		Offer savedOffer = offers.get(0);
		assertNotNull(savedOffer);
		assertEquals(OfferImpl.class, savedOffer.getClass());
		if(savedOffer instanceof OfferImpl) {
			OfferImpl offerImpl = (OfferImpl)savedOffer;
			assertEquals("token1", offerImpl.getToken());
		}
		assertNotNull(savedOffer.getValidFrom());
		assertNotNull(savedOffer.getValidTo());
		assertEquals(cancellingFeeAmount.doubleValue(), savedOffer.getCancellingFee().getAmount().doubleValue(), 0.001);
		assertEquals("USD", savedOffer.getCancellingFee().getCurrencyCode());
		assertEquals(Integer.valueOf(10), savedOffer.getCancellingFeeDeadlineDays());
		assertEquals(testOres.getResourceableId(), savedOffer.getResourceId());
		assertEquals(testOres.getResourceableTypeName(), savedOffer.getResourceTypeName());
		assertEquals("TestSaveOffer", savedOffer.getResourceDisplayName());
	}
	
	@Test
	public void addCostCenter() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestSaveOffer");
		CostCenter center = acService.createCostCenter();
		acOfferManager.save(offer, center);
		dbInstance.commitAndCloseSession();
		
		Offer reloadedOffer = acOfferManager.findOfferByResource(testOres, true, null, false, null, null).get(0);
		assertEquals(center, reloadedOffer.getCostCenter());
	}
	
	@Test
	public void getCostCenterKeyToOfferCount() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();
		
		CostCenter center1 = acService.createCostCenter();
		CostCenter center2 = acService.createCostCenter();
		CostCenter center3 = acService.createCostCenter();
		CostCenter center4 = acService.createCostCenter();
		Offer offer11 = acOfferManager.createOffer(testOres, "TestSaveOffer");
		Offer offer12 = acOfferManager.createOffer(testOres, "TestSaveOffer");
		Offer offer21 = acOfferManager.createOffer(testOres, "TestSaveOffer");
		Offer offer41 = acOfferManager.createOffer(testOres, "TestSaveOffer");
		acOfferManager.save(offer11, center1);
		acOfferManager.save(offer12, center1);
		acOfferManager.save(offer21, center2);
		acOfferManager.save(offer41, center4);
		dbInstance.commitAndCloseSession();
		
		Map<Long, Long> centerKeyToOfferCount = acOfferManager.getCostCenterKeyToOfferCount(List.of(center1, center2, center3));
		
		assertEquals(Long.valueOf(2), centerKeyToOfferCount.get(center1.getKey()));
		assertEquals(Long.valueOf(1), centerKeyToOfferCount.get(center2.getKey()));
		assertNull(centerKeyToOfferCount.get(center3.getKey()));
		assertNull(centerKeyToOfferCount.get(center4.getKey()));
	}

	@Test
	public void testDeleteOffer() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestDeleteOffer");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//create a link offer to method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class);
		AccessMethod method = methods.get(0);
		OfferAccess access = acMethodManager.createOfferAccess(offer, method);
		acMethodManager.save(access);

		dbInstance.commitAndCloseSession();

		//retrieve the offer
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null, false, null, null);
		assertNotNull(offers);
		assertEquals(1, offers.size());
		assertEquals(offer, offers.get(0));
		dbInstance.commitAndCloseSession();

		//delete the offer
		acOfferManager.deleteOffer(offer);
		dbInstance.commitAndCloseSession();

		//try to retrieve the offer
		List<Offer> noOffers = acOfferManager.findOfferByResource(testOres, true, null, false, null, null);
		assertNotNull(noOffers);
		assertEquals(0, noOffers.size());
		dbInstance.commitAndCloseSession();

		//retrieve all offers, deleted too
		List<Offer> delOffers = acOfferManager.findOfferByResource(testOres, false, null, false, null, null);
		assertNotNull(delOffers);
		assertEquals(1, delOffers.size());
		assertEquals(offer, delOffers.get(0));
		assertEquals(false, delOffers.get(0).isValid());
		dbInstance.commitAndCloseSession();
	}

	@Test
	public void testDeleteResource() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		//create an offer
		Offer offer = acOfferManager.createOffer(testOres, "TestDeleteResource");
		assertNotNull(offer);
		assertEquals(OfferImpl.class, offer.getClass());
		//and save the offer
		acOfferManager.saveOffer(offer);

		dbInstance.commitAndCloseSession();

		//delete the resource
		testOres = dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, testOres.getKey());
		dbInstance.deleteObject(testOres);

		dbInstance.commitAndCloseSession();

		//load offer by resource -> nothing found
		List<Offer> retrievedOffers = acOfferManager.findOfferByResource(testOres, true, null, false, null, null);
		assertNotNull(retrievedOffers);
		assertEquals(0, retrievedOffers.size());

		//load offer by key -> found and loaded without error
		Offer retrievedOffer = acOfferManager.loadOfferByKey(offer.getKey());
		assertNotNull(retrievedOffer);
		assertNull(retrievedOffer.getResource());
		assertEquals(offer, retrievedOffer);
	}
	
	@Test
	public void shouldFilterByValid() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		AccessMethod method = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		
		// Offer valid
		Offer offerValid = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerValid = acOfferManager.saveOffer(offerValid);
		acMethodManager.save(acMethodManager.createOfferAccess(offerValid, method));
		// Offer not valid
		Offer offerNotValid = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerNotValid = acOfferManager.saveOffer(offerNotValid);
		acMethodManager.save(acMethodManager.createOfferAccess(offerNotValid, method));
		acOfferManager.deleteOffer(offerNotValid); // valid => false
		dbInstance.commitAndCloseSession();
		
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null, false, null, null);
		
		assertThat(offers).hasSize(1).containsExactlyInAnyOrder(offerValid);
	}
	
	@Test
	public void shouldFilterByDate() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		AccessMethod method = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		Date date = new Date();
		
		// Offer valid always
		Offer offerAlways = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerAlways = acOfferManager.saveOffer(offerAlways);
		acMethodManager.save(acMethodManager.createOfferAccess(offerAlways, method));
		// Offer valid at due date
		Offer offerInRange = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInRange.setValidFrom(DateUtils.addDays(date, -2));
		offerInRange.setValidTo(DateUtils.addDays(date, 2));
		offerInRange = acOfferManager.saveOffer(offerInRange);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInRange, method));
		// Offer valid in past
		Offer offerInPast = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInPast.setValidFrom(DateUtils.addDays(date, -10));
		offerInPast.setValidTo(DateUtils.addDays(date, -2));
		offerInPast = acOfferManager.saveOffer(offerInPast);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInPast, method));
		// Offer valid in future
		Offer offerInFuture = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInFuture.setValidFrom(DateUtils.addDays(date, 2));
		offerInFuture.setValidTo(DateUtils.addDays(date, 12));
		offerInFuture = acOfferManager.saveOffer(offerInFuture);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInFuture, method));
		dbInstance.commitAndCloseSession();
		
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, date, false, null, null);
		assertThat(offers).containsExactlyInAnyOrder(offerAlways, offerInRange);
		
		offers = acOfferManager.findOfferByResource(testOres, true, date, true, null, null);
		assertThat(offers).containsExactlyInAnyOrder(offerInRange);
	}
	
	@Test
	public void shouldFilterByWebPublish() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		AccessMethod method = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		
		// Offer not web publish
		Offer offerNotWebPublish = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerNotWebPublish = acOfferManager.saveOffer(offerNotWebPublish);
		acMethodManager.save(acMethodManager.createOfferAccess(offerNotWebPublish, method));
		// Offer web published
		Offer offerWebPublish = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerWebPublish.setCatalogWebPublish(true);
		offerWebPublish = acOfferManager.saveOffer(offerWebPublish);
		dbInstance.commitAndCloseSession();
		
		assertThat(acOfferManager.findOfferByResource(testOres, true, null, false, null, null)).containsExactlyInAnyOrder(offerNotWebPublish, offerWebPublish);
		assertThat(acOfferManager.findOfferByResource(testOres, true, null, false, Boolean.TRUE, null)).containsExactlyInAnyOrder(offerWebPublish);
		assertThat(acOfferManager.findOfferByResource(testOres, true, null, false, Boolean.FALSE, null)).containsExactlyInAnyOrder(offerNotWebPublish);
	}
	
	@Test
	public void shouldFilterByOrganisation() {
		OLATResource testOres = JunitTestHelper.createRandomResource();
		AccessMethod method = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), organisation1, null, JunitTestHelper.getDefaultActor());
		Organisation organisationOther = organisationService.createOrganisation(random(), null, random(), organisation1, null, JunitTestHelper.getDefaultActor());
		
		// Offer in organisation
		Offer offerInOrganisation1 = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInOrganisation1 = acOfferManager.saveOffer(offerInOrganisation1);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInOrganisation1, method));
		offerToOrganisationDao.createRelation(offerInOrganisation1, organisation1);
		Offer offerInOrganisation2 = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInOrganisation2 = acOfferManager.saveOffer(offerInOrganisation2);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInOrganisation2, method));
		offerToOrganisationDao.createRelation(offerInOrganisation2, organisation2);
		// Offer not in organisation
		Offer offerNotInOrganisation = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerNotInOrganisation = acOfferManager.saveOffer(offerNotInOrganisation);
		acMethodManager.save(acMethodManager.createOfferAccess(offerNotInOrganisation, method));
		offerToOrganisationDao.createRelation(offerNotInOrganisation, organisationOther);
		// Offer in no organisation
		Offer offerInNoOrganisation = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerInNoOrganisation = acOfferManager.saveOffer(offerInNoOrganisation);
		acMethodManager.save(acMethodManager.createOfferAccess(offerInNoOrganisation, method));
		dbInstance.commitAndCloseSession();
		
		List<Offer> offers = acOfferManager.findOfferByResource(testOres, true, null, false, null, List.of(organisation1, organisation2));
		
		assertThat(offers).containsExactlyInAnyOrder(offerInOrganisation2, offerInOrganisation1);
	}
	
	@Test
	public void shouldGetOpenAccessible() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), organisation1, null, JunitTestHelper.getDefaultActor());
		Organisation organisation3 = organisationService.createOrganisation(random(), null, random(), organisation2, null, JunitTestHelper.getDefaultActor());
		List<Organisation> organisations = List.of(organisation1, organisation2);
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();
		
		// No offer
		assertThat(acOfferManager.isOpenAccessible(resource, null, organisations)).isFalse();
		
		// No open offer
		Offer offer = acService.createOffer(resource, random());
		acOfferManager.saveOffer(offer);
		acService.updateOfferOrganisations(offer, organisations);
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isOpenAccessible(resource, null, organisations)).isFalse();
		
		// Open offer in other organisation
		offer = acService.createOffer(resource, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		acService.updateOfferOrganisations(offer, List.of(organisation3));
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isOpenAccessible(resource, null, organisations)).isFalse();
		
		// Open offer in organisation
		acService.updateOfferOrganisations(offer, List.of(organisation2));
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isOpenAccessible(resource, null, organisations)).isTrue();
		
		// Delete offer
		acService.deleteOffer(offer);
		assertThat(acOfferManager.isOpenAccessible(resource, null, organisations)).isFalse();
	}
	
	@Test
	public void shouldGetOpenAccessible_filterWebPublish() {
		Organisation organisation = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		OLATResource testOres = JunitTestHelper.createRandomResource();
		
		Offer offerWebPublish = acOfferManager.createOffer(testOres, JunitTestHelper.miniRandom());
		offerWebPublish.setOpenAccess(true);
		offerWebPublish.setCatalogWebPublish(true);
		offerWebPublish = acOfferManager.saveOffer(offerWebPublish);
		acService.updateOfferOrganisations(offerWebPublish, List.of(organisation));
		dbInstance.commitAndCloseSession();
		
		assertThat(acOfferManager.isOpenAccessible(testOres, null, null)).isTrue();
		assertThat(acOfferManager.isOpenAccessible(testOres, Boolean.TRUE, null)).isTrue();
		assertThat(acOfferManager.isOpenAccessible(testOres, Boolean.FALSE, null)).isFalse();
	}
	
	@Test
	public void shouldGetOpenAccessibleResources() {
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		OLATResource resource3 = JunitTestHelper.createRandomResource();
		OLATResource resource4 = JunitTestHelper.createRandomResource();
		OLATResource resource5 = JunitTestHelper.createRandomResource();
		
		// Open access offer
		Offer offer = acOfferManager.createOffer(resource1, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		
		// Resource with two open access offers
		offer = acOfferManager.createOffer(resource2, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		offer = acOfferManager.createOffer(resource2, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		
		// Offer but not open access
		offer = acOfferManager.createOffer(resource4, random());
		offer.setOpenAccess(false);
		acOfferManager.saveOffer(offer);
		
		// Not in selection list
		offer = acOfferManager.createOffer(resource5, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		dbInstance.commitAndCloseSession();
		
		List<OLATResource> resources = acOfferManager.loadOpenAccessibleResources(List.of(resource1, resource2, resource3, resource4), null, null);
		
		assertThat(resources).hasSize(2).containsExactlyInAnyOrder(resource1, resource2);
	}
	
	@Test
	public void shouldGetOpenAccessibleResources_filterWebPublish() {
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		AccessMethod method = acMethodManager.getAvailableMethodsByType(TokenAccessMethod.class).get(0);
		
		// Offer not web publish
		Offer offerNotWebPublish = acOfferManager.createOffer(resource1, JunitTestHelper.miniRandom());
		offerNotWebPublish.setOpenAccess(true);
		offerNotWebPublish = acOfferManager.saveOffer(offerNotWebPublish);
		acMethodManager.save(acMethodManager.createOfferAccess(offerNotWebPublish, method));
		// Offer web published
		Offer offerWebPublish = acOfferManager.createOffer(resource2, JunitTestHelper.miniRandom());
		offerWebPublish.setOpenAccess(true);
		offerWebPublish.setCatalogWebPublish(true);
		offerWebPublish = acOfferManager.saveOffer(offerWebPublish);
		dbInstance.commitAndCloseSession();
		
		assertThat(acOfferManager.loadOpenAccessibleResources(List.of(resource1, resource2), null, null)).containsExactlyInAnyOrder(resource1, resource2);
		assertThat(acOfferManager.loadOpenAccessibleResources(List.of(resource1, resource2), Boolean.TRUE, null)).containsExactlyInAnyOrder(resource2);
		assertThat(acOfferManager.loadOpenAccessibleResources(List.of(resource1, resource2), Boolean.FALSE, null)).containsExactlyInAnyOrder(resource1);
	}
	
	@Test
	public void shouldGetOpenAccessibleResources_filterOrganisations() {
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		Organisation otherOganisation = organisationService.createOrganisation(random(), null, random(), null, null, JunitTestHelper.getDefaultActor());
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		OLATResource resource3 = JunitTestHelper.createRandomResource();
		OLATResource resource4 = JunitTestHelper.createRandomResource();
		
		createReOpenAccess(resource1, singletonList(organisation1));
		createReOpenAccess(resource2, singletonList(organisation2));
		createReOpenAccess(resource4, singletonList(otherOganisation));
		dbInstance.commitAndCloseSession();
		
		List<OLATResource> resources = acOfferManager.loadOpenAccessibleResources(List.of(resource1, resource2, resource3, resource4), null, List.of(organisation1, organisation2));
		
		assertThat(resources).hasSize(2).containsExactlyInAnyOrder(resource1, resource2);
	}
	
	private void createReOpenAccess(OLATResource resource, List<Organisation> offerOrganisations) {
		Offer offer = acService.createOffer(resource, random());
		offer.setOpenAccess(true);
		offer = acService.save(offer);
		acService.updateOfferOrganisations(offer, offerOrganisations);
	}
	
	@Test
	public void shouldGetGuestAccessible() {
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();
		
		// No offer
		assertThat(acOfferManager.isGuestAccessible(resource)).isFalse();
		
		// Open Access
		Offer offer = acService.createOffer(resource, random());
		offer.setOpenAccess(true);
		acOfferManager.saveOffer(offer);
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isGuestAccessible(resource)).isFalse();
		
		// Guest access
		offer = acService.createOffer(resource, random());
		offer.setGuestAccess(true);
		offer = acOfferManager.saveOffer(offer);
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isGuestAccessible(resource)).isTrue();
		
		// Delete guest access
		acService.deleteOffer(offer);
		dbInstance.commitAndCloseSession();
		assertThat(acOfferManager.isGuestAccessible(resource)).isFalse();
	}
	
	@Test
	public void shouldGetGuestAccessibleResources() {
		OLATResource resource1 = JunitTestHelper.createRandomResource();
		OLATResource resource2 = JunitTestHelper.createRandomResource();
		OLATResource resource3 = JunitTestHelper.createRandomResource();
		OLATResource resource4 = JunitTestHelper.createRandomResource();
		OLATResource resource5 = JunitTestHelper.createRandomResource();
		
		// Open access offer
		Offer offer = acOfferManager.createOffer(resource1, random());
		offer.setGuestAccess(true);
		acOfferManager.saveOffer(offer);
		
		// Resource with two open access offers
		offer = acOfferManager.createOffer(resource2, random());
		offer.setGuestAccess(true);
		acOfferManager.saveOffer(offer);
		offer = acOfferManager.createOffer(resource2, random());
		offer.setGuestAccess(true);
		acOfferManager.saveOffer(offer);
		
		// Offer but not open access
		offer = acOfferManager.createOffer(resource4, random());
		offer.setGuestAccess(false);
		acOfferManager.saveOffer(offer);
		
		// Not in selection list
		offer = acOfferManager.createOffer(resource5, random());
		offer.setGuestAccess(true);
		acOfferManager.saveOffer(offer);
		dbInstance.commitAndCloseSession();
		
		List<OLATResource> resources = acOfferManager.loadGuestAccessibleResources(List.of(resource1, resource2, resource3, resource4));
		
		assertThat(resources).hasSize(2).containsExactlyInAnyOrder(resource1, resource2);
	}

}
