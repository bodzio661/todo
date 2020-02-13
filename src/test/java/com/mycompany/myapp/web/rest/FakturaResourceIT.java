package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.TodoApp;
import com.mycompany.myapp.domain.Faktura;
import com.mycompany.myapp.repository.FakturaRepository;
import com.mycompany.myapp.repository.search.FakturaSearchRepository;
import com.mycompany.myapp.service.FakturaService;
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static com.mycompany.myapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.domain.enumeration.Type;
import com.mycompany.myapp.domain.enumeration.Status;
import com.mycompany.myapp.domain.enumeration.Zaleglosc;
/**
 * Integration tests for the {@link FakturaResource} REST controller.
 */
@SpringBootTest(classes = TodoApp.class)
public class FakturaResourceIT {

    private static final String DEFAULT_NUMER_FAKTURY = "AAAAAAAAAA";
    private static final String UPDATED_NUMER_FAKTURY = "BBBBBBBBBB";

    private static final Float DEFAULT_KWOTA_FAKTURY = 1F;
    private static final Float UPDATED_KWOTA_FAKTURY = 2F;

    private static final LocalDate DEFAULT_DATA_FAKTURY = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA_FAKTURY = LocalDate.now(ZoneId.systemDefault());

    private static final Type DEFAULT_TYP_FAKTURY = Type.Kosztowa;
    private static final Type UPDATED_TYP_FAKTURY = Type.Przychodowa;

    private static final Status DEFAULT_STATUS_FAKTURY = Status.Zaplacone;
    private static final Status UPDATED_STATUS_FAKTURY = Status.Nowe;

    private static final Zaleglosc DEFAULT_ZALEGLOSC_FAKTURY = Zaleglosc.OK;
    private static final Zaleglosc UPDATED_ZALEGLOSC_FAKTURY = Zaleglosc.Zalegla;

    @Autowired
    private FakturaRepository fakturaRepository;

    @Autowired
    private FakturaService fakturaService;

    /**
     * This repository is mocked in the com.mycompany.myapp.repository.search test package.
     *
     * @see com.mycompany.myapp.repository.search.FakturaSearchRepositoryMockConfiguration
     */
    @Autowired
    private FakturaSearchRepository mockFakturaSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restFakturaMockMvc;

    private Faktura faktura;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final FakturaResource fakturaResource = new FakturaResource(fakturaService);
        this.restFakturaMockMvc = MockMvcBuilders.standaloneSetup(fakturaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Faktura createEntity(EntityManager em) {
        Faktura faktura = new Faktura()
            .numerFaktury(DEFAULT_NUMER_FAKTURY)
            .kwotaFaktury(DEFAULT_KWOTA_FAKTURY)
            .dataFaktury(DEFAULT_DATA_FAKTURY)
            .typFaktury(DEFAULT_TYP_FAKTURY)
            .statusFaktury(DEFAULT_STATUS_FAKTURY)
            .zalegloscFaktury(DEFAULT_ZALEGLOSC_FAKTURY);
        return faktura;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Faktura createUpdatedEntity(EntityManager em) {
        Faktura faktura = new Faktura()
            .numerFaktury(UPDATED_NUMER_FAKTURY)
            .kwotaFaktury(UPDATED_KWOTA_FAKTURY)
            .dataFaktury(UPDATED_DATA_FAKTURY)
            .typFaktury(UPDATED_TYP_FAKTURY)
            .statusFaktury(UPDATED_STATUS_FAKTURY)
            .zalegloscFaktury(UPDATED_ZALEGLOSC_FAKTURY);
        return faktura;
    }

    @BeforeEach
    public void initTest() {
        faktura = createEntity(em);
    }

    @Test
    @Transactional
    public void createFaktura() throws Exception {
        int databaseSizeBeforeCreate = fakturaRepository.findAll().size();

        // Create the Faktura
        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isCreated());

        // Validate the Faktura in the database
        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeCreate + 1);
        Faktura testFaktura = fakturaList.get(fakturaList.size() - 1);
        assertThat(testFaktura.getNumerFaktury()).isEqualTo(DEFAULT_NUMER_FAKTURY);
        assertThat(testFaktura.getKwotaFaktury()).isEqualTo(DEFAULT_KWOTA_FAKTURY);
        assertThat(testFaktura.getDataFaktury()).isEqualTo(DEFAULT_DATA_FAKTURY);
        assertThat(testFaktura.getTypFaktury()).isEqualTo(DEFAULT_TYP_FAKTURY);
        assertThat(testFaktura.getStatusFaktury()).isEqualTo(DEFAULT_STATUS_FAKTURY);
        assertThat(testFaktura.getZalegloscFaktury()).isEqualTo(DEFAULT_ZALEGLOSC_FAKTURY);

        // Validate the Faktura in Elasticsearch
        verify(mockFakturaSearchRepository, times(1)).save(testFaktura);
    }

    @Test
    @Transactional
    public void createFakturaWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = fakturaRepository.findAll().size();

        // Create the Faktura with an existing ID
        faktura.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        // Validate the Faktura in the database
        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeCreate);

        // Validate the Faktura in Elasticsearch
        verify(mockFakturaSearchRepository, times(0)).save(faktura);
    }


    @Test
    @Transactional
    public void checkNumerFakturyIsRequired() throws Exception {
        int databaseSizeBeforeTest = fakturaRepository.findAll().size();
        // set the field null
        faktura.setNumerFaktury(null);

        // Create the Faktura, which fails.

        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkKwotaFakturyIsRequired() throws Exception {
        int databaseSizeBeforeTest = fakturaRepository.findAll().size();
        // set the field null
        faktura.setKwotaFaktury(null);

        // Create the Faktura, which fails.

        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDataFakturyIsRequired() throws Exception {
        int databaseSizeBeforeTest = fakturaRepository.findAll().size();
        // set the field null
        faktura.setDataFaktury(null);

        // Create the Faktura, which fails.

        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypFakturyIsRequired() throws Exception {
        int databaseSizeBeforeTest = fakturaRepository.findAll().size();
        // set the field null
        faktura.setTypFaktury(null);

        // Create the Faktura, which fails.

        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusFakturyIsRequired() throws Exception {
        int databaseSizeBeforeTest = fakturaRepository.findAll().size();
        // set the field null
        faktura.setStatusFaktury(null);

        // Create the Faktura, which fails.

        restFakturaMockMvc.perform(post("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllFakturas() throws Exception {
        // Initialize the database
        fakturaRepository.saveAndFlush(faktura);

        // Get all the fakturaList
        restFakturaMockMvc.perform(get("/api/fakturas?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(faktura.getId().intValue())))
            .andExpect(jsonPath("$.[*].numerFaktury").value(hasItem(DEFAULT_NUMER_FAKTURY)))
            .andExpect(jsonPath("$.[*].kwotaFaktury").value(hasItem(DEFAULT_KWOTA_FAKTURY.doubleValue())))
            .andExpect(jsonPath("$.[*].dataFaktury").value(hasItem(DEFAULT_DATA_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].typFaktury").value(hasItem(DEFAULT_TYP_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].statusFaktury").value(hasItem(DEFAULT_STATUS_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].zalegloscFaktury").value(hasItem(DEFAULT_ZALEGLOSC_FAKTURY.toString())));
    }
    
    @Test
    @Transactional
    public void getFaktura() throws Exception {
        // Initialize the database
        fakturaRepository.saveAndFlush(faktura);

        // Get the faktura
        restFakturaMockMvc.perform(get("/api/fakturas/{id}", faktura.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(faktura.getId().intValue()))
            .andExpect(jsonPath("$.numerFaktury").value(DEFAULT_NUMER_FAKTURY))
            .andExpect(jsonPath("$.kwotaFaktury").value(DEFAULT_KWOTA_FAKTURY.doubleValue()))
            .andExpect(jsonPath("$.dataFaktury").value(DEFAULT_DATA_FAKTURY.toString()))
            .andExpect(jsonPath("$.typFaktury").value(DEFAULT_TYP_FAKTURY.toString()))
            .andExpect(jsonPath("$.statusFaktury").value(DEFAULT_STATUS_FAKTURY.toString()))
            .andExpect(jsonPath("$.zalegloscFaktury").value(DEFAULT_ZALEGLOSC_FAKTURY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingFaktura() throws Exception {
        // Get the faktura
        restFakturaMockMvc.perform(get("/api/fakturas/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateFaktura() throws Exception {
        // Initialize the database
        fakturaService.save(faktura);
        // As the test used the service layer, reset the Elasticsearch mock repository
        reset(mockFakturaSearchRepository);

        int databaseSizeBeforeUpdate = fakturaRepository.findAll().size();

        // Update the faktura
        Faktura updatedFaktura = fakturaRepository.findById(faktura.getId()).get();
        // Disconnect from session so that the updates on updatedFaktura are not directly saved in db
        em.detach(updatedFaktura);
        updatedFaktura
            .numerFaktury(UPDATED_NUMER_FAKTURY)
            .kwotaFaktury(UPDATED_KWOTA_FAKTURY)
            .dataFaktury(UPDATED_DATA_FAKTURY)
            .typFaktury(UPDATED_TYP_FAKTURY)
            .statusFaktury(UPDATED_STATUS_FAKTURY)
            .zalegloscFaktury(UPDATED_ZALEGLOSC_FAKTURY);

        restFakturaMockMvc.perform(put("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedFaktura)))
            .andExpect(status().isOk());

        // Validate the Faktura in the database
        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeUpdate);
        Faktura testFaktura = fakturaList.get(fakturaList.size() - 1);
        assertThat(testFaktura.getNumerFaktury()).isEqualTo(UPDATED_NUMER_FAKTURY);
        assertThat(testFaktura.getKwotaFaktury()).isEqualTo(UPDATED_KWOTA_FAKTURY);
        assertThat(testFaktura.getDataFaktury()).isEqualTo(UPDATED_DATA_FAKTURY);
        assertThat(testFaktura.getTypFaktury()).isEqualTo(UPDATED_TYP_FAKTURY);
        assertThat(testFaktura.getStatusFaktury()).isEqualTo(UPDATED_STATUS_FAKTURY);
        assertThat(testFaktura.getZalegloscFaktury()).isEqualTo(UPDATED_ZALEGLOSC_FAKTURY);

        // Validate the Faktura in Elasticsearch
        verify(mockFakturaSearchRepository, times(1)).save(testFaktura);
    }

    @Test
    @Transactional
    public void updateNonExistingFaktura() throws Exception {
        int databaseSizeBeforeUpdate = fakturaRepository.findAll().size();

        // Create the Faktura

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFakturaMockMvc.perform(put("/api/fakturas")
            .contentType(TestUtil.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(faktura)))
            .andExpect(status().isBadRequest());

        // Validate the Faktura in the database
        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Faktura in Elasticsearch
        verify(mockFakturaSearchRepository, times(0)).save(faktura);
    }

    @Test
    @Transactional
    public void deleteFaktura() throws Exception {
        // Initialize the database
        fakturaService.save(faktura);

        int databaseSizeBeforeDelete = fakturaRepository.findAll().size();

        // Delete the faktura
        restFakturaMockMvc.perform(delete("/api/fakturas/{id}", faktura.getId())
            .accept(TestUtil.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Faktura> fakturaList = fakturaRepository.findAll();
        assertThat(fakturaList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Faktura in Elasticsearch
        verify(mockFakturaSearchRepository, times(1)).deleteById(faktura.getId());
    }

    @Test
    @Transactional
    public void searchFaktura() throws Exception {
        // Initialize the database
        fakturaService.save(faktura);
        when(mockFakturaSearchRepository.search(queryStringQuery("id:" + faktura.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(faktura), PageRequest.of(0, 1), 1));
        // Search the faktura
        restFakturaMockMvc.perform(get("/api/_search/fakturas?query=id:" + faktura.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(faktura.getId().intValue())))
            .andExpect(jsonPath("$.[*].numerFaktury").value(hasItem(DEFAULT_NUMER_FAKTURY)))
            .andExpect(jsonPath("$.[*].kwotaFaktury").value(hasItem(DEFAULT_KWOTA_FAKTURY.doubleValue())))
            .andExpect(jsonPath("$.[*].dataFaktury").value(hasItem(DEFAULT_DATA_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].typFaktury").value(hasItem(DEFAULT_TYP_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].statusFaktury").value(hasItem(DEFAULT_STATUS_FAKTURY.toString())))
            .andExpect(jsonPath("$.[*].zalegloscFaktury").value(hasItem(DEFAULT_ZALEGLOSC_FAKTURY.toString())));
    }
}
