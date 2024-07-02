
DROP TABLE [Drive]
go

DROP TABLE [CourierRequest]
go

DROP TABLE [Offer]
go

DROP TABLE [Package]
go

DROP TABLE [Courier]
go

DROP TABLE [Vehicle]
go

DROP TABLE [District]
go

DROP TABLE [City]
go

DROP TABLE [Admin]
go

DROP TABLE [UserApp]
go

CREATE TABLE [Admin]
( 
	[idUser]             integer  NOT NULL 
)
go

CREATE TABLE [City]
( 
	[idCity]             integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL ,
	[PostanskiBroj]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [Courier]
( 
	[idUser]             integer  NOT NULL ,
	[idVehicle]          integer  NOT NULL ,
	[BrojIsporucenihPaketa] integer  NOT NULL 
	CONSTRAINT [Zero_927686161]
		 DEFAULT  0,
	[OstvarenProfit]     decimal(10,3)  NOT NULL 
	CONSTRAINT [Zero_2023247848]
		 DEFAULT  0,
	[Status]             integer  NOT NULL 
)
go

CREATE TABLE [CourierRequest]
( 
	[idCourierRequest]   integer  IDENTITY  NOT NULL ,
	[idUser]             integer  NOT NULL ,
	[idVehicle]          integer  NOT NULL 
)
go

CREATE TABLE [District]
( 
	[idDistrict]         integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(20)  NOT NULL ,
	[X_Kordinata]        integer  NOT NULL ,
	[Y_Kordinata]        integer  NOT NULL ,
	[idCity]             integer  NOT NULL 
)
go

CREATE TABLE [Drive]
( 
	[idDrive]            integer  IDENTITY  NOT NULL ,
	[Distanca]           integer  NOT NULL ,
	[idUser]             integer  NOT NULL ,
	[BrojPaketa]         integer  NOT NULL ,
	[ProfitVoznje]       decimal(10,3)  NOT NULL ,
	[idDistrict]         integer  NOT NULL 
)
go

CREATE TABLE [Offer]
( 
	[idOffer]            integer  IDENTITY  NOT NULL ,
	[idPackage]          integer  NOT NULL ,
	[idUser]             integer  NOT NULL ,
	[ProcenatCeneIsporuke] decimal(10,3)  NOT NULL 
	CONSTRAINT [Positive_529634655]
		CHECK  ( ProcenatCeneIsporuke >= 0 ),
	[StatusPonude]       integer  NOT NULL 
)
go

CREATE TABLE [Package]
( 
	[idPackage]          integer  IDENTITY  NOT NULL ,
	[Tip]                integer  NOT NULL 
	CONSTRAINT [ZahtevStatus_368292716]
		CHECK  ( [Tip]=0 OR [Tip]=1 OR [Tip]=2 ),
	[OpstinaDostavlja]   integer  NOT NULL ,
	[OpstinaPreuzima]    integer  NOT NULL ,
	[TezinaPaketa]       decimal(10,3)  NOT NULL ,
	[StatusIsporuke]     integer  NOT NULL 
	CONSTRAINT [PaketStatus_891244643]
		CHECK  ( [StatusIsporuke]=0 OR [StatusIsporuke]=1 OR [StatusIsporuke]=2 OR [StatusIsporuke]=3 ),
	[idUser]             integer  NOT NULL ,
	[Cena]               decimal(10,3)  NOT NULL ,
	[VremePrihvatanja]   datetime  NULL ,
	[VremeStvaranja]     datetime  NOT NULL 
)
go

CREATE TABLE [UserApp]
( 
	[idUser]             integer  IDENTITY  NOT NULL ,
	[Ime]                varchar(20)  NOT NULL ,
	[Prezime]            varchar(20)  NOT NULL ,
	[KorisnickoIme]      varchar(20)  NOT NULL ,
	[Sifra]              varchar(20)  NOT NULL ,
	[BrojPoslatihPaketa] integer  NOT NULL 
	CONSTRAINT [Zero_169894370]
		 DEFAULT  0
)
go

CREATE TABLE [Vehicle]
( 
	[idVehicle]          integer  IDENTITY  NOT NULL ,
	[RegistracioniBroj]  varchar(100)  NOT NULL ,
	[TipGoriva]          integer  NOT NULL 
	CONSTRAINT [TipGoriva_2023404358]
		CHECK  ( [TipGoriva]=0 OR [TipGoriva]=1 OR [TipGoriva]=2 ),
	[Potrosnja]          decimal(10,3)  NOT NULL 
)
go

ALTER TABLE [Admin]
	ADD CONSTRAINT [XPKAdmin] PRIMARY KEY  CLUSTERED ([idUser] ASC)
go

ALTER TABLE [City]
	ADD CONSTRAINT [XPKCity] PRIMARY KEY  CLUSTERED ([idCity] ASC)
go

ALTER TABLE [Courier]
	ADD CONSTRAINT [XPKCourier] PRIMARY KEY  CLUSTERED ([idUser] ASC)
go

ALTER TABLE [CourierRequest]
	ADD CONSTRAINT [XPKCourierRequest] PRIMARY KEY  CLUSTERED ([idCourierRequest] ASC)
go

ALTER TABLE [District]
	ADD CONSTRAINT [XPKAddress] PRIMARY KEY  CLUSTERED ([idDistrict] ASC)
go

ALTER TABLE [Drive]
	ADD CONSTRAINT [XPKDrive] PRIMARY KEY  CLUSTERED ([idDrive] ASC)
go

ALTER TABLE [Offer]
	ADD CONSTRAINT [XPKOffer] PRIMARY KEY  CLUSTERED ([idOffer] ASC)
go

ALTER TABLE [Package]
	ADD CONSTRAINT [XPKPackage] PRIMARY KEY  CLUSTERED ([idPackage] ASC)
go

ALTER TABLE [UserApp]
	ADD CONSTRAINT [XPKUser] PRIMARY KEY  CLUSTERED ([idUser] ASC)
go

ALTER TABLE [Vehicle]
	ADD CONSTRAINT [XPKVehicle] PRIMARY KEY  CLUSTERED ([idVehicle] ASC)
go


ALTER TABLE [Admin]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([idUser]) REFERENCES [UserApp]([idUser])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Courier]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([idUser]) REFERENCES [UserApp]([idUser])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Courier]
	ADD CONSTRAINT [R_9] FOREIGN KEY ([idVehicle]) REFERENCES [Vehicle]([idVehicle])
		ON UPDATE CASCADE
go


ALTER TABLE [CourierRequest]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([idUser]) REFERENCES [UserApp]([idUser])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [CourierRequest]
	ADD CONSTRAINT [R_25] FOREIGN KEY ([idVehicle]) REFERENCES [Vehicle]([idVehicle])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [District]
	ADD CONSTRAINT [R_15] FOREIGN KEY ([idCity]) REFERENCES [City]([idCity])
		ON UPDATE CASCADE
go


ALTER TABLE [Drive]
	ADD CONSTRAINT [R_27] FOREIGN KEY ([idUser]) REFERENCES [Courier]([idUser])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Drive]
	ADD CONSTRAINT [R_28] FOREIGN KEY ([idDistrict]) REFERENCES [District]([idDistrict])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Offer]
	ADD CONSTRAINT [R_12] FOREIGN KEY ([idPackage]) REFERENCES [Package]([idPackage])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Offer]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([idUser]) REFERENCES [Courier]([idUser])
		ON UPDATE CASCADE
go


ALTER TABLE [Package]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([OpstinaPreuzima]) REFERENCES [District]([idDistrict])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Package]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([OpstinaDostavlja]) REFERENCES [District]([idDistrict])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Package]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([idUser]) REFERENCES [Courier]([idUser])
		ON UPDATE NO ACTION
go
