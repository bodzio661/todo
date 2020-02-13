import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { FakturaService } from 'app/entities/faktura/faktura.service';
import { IFaktura, Faktura } from 'app/shared/model/faktura.model';
import { Type } from 'app/shared/model/enumerations/type.model';
import { Status } from 'app/shared/model/enumerations/status.model';
import { Zaleglosc } from 'app/shared/model/enumerations/zaleglosc.model';

describe('Service Tests', () => {
  describe('Faktura Service', () => {
    let injector: TestBed;
    let service: FakturaService;
    let httpMock: HttpTestingController;
    let elemDefault: IFaktura;
    let expectedResult: IFaktura | IFaktura[] | boolean | null;
    let currentDate: moment.Moment;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(FakturaService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new Faktura(0, 'AAAAAAA', 0, currentDate, Type.Kosztowa, Status.Zaplacone, Zaleglosc.OK);
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            dataFaktury: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Faktura', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            dataFaktury: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            dataFaktury: currentDate
          },
          returnedFromService
        );

        service.create(new Faktura()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Faktura', () => {
        const returnedFromService = Object.assign(
          {
            numerFaktury: 'BBBBBB',
            kwotaFaktury: 1,
            dataFaktury: currentDate.format(DATE_FORMAT),
            typFaktury: 'BBBBBB',
            statusFaktury: 'BBBBBB',
            zalegloscFaktury: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            dataFaktury: currentDate
          },
          returnedFromService
        );

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Faktura', () => {
        const returnedFromService = Object.assign(
          {
            numerFaktury: 'BBBBBB',
            kwotaFaktury: 1,
            dataFaktury: currentDate.format(DATE_FORMAT),
            typFaktury: 'BBBBBB',
            statusFaktury: 'BBBBBB',
            zalegloscFaktury: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            dataFaktury: currentDate
          },
          returnedFromService
        );

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Faktura', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
