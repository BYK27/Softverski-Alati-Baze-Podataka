
CREATE TRIGGER TR_TransportOffer_
   ON Offer
   AFTER UPDATE
AS 
BEGIN
	DECLARE @idPackage INT;
    DECLARE @newStatusPonude INT;

	select @idPackage = idPackage, @newStatusPonude = StatusPonude
    FROM inserted;

    IF @newStatusPonude = 1
    BEGIN
        DELETE FROM Offer
        WHERE idPackage = @idPackage AND StatusPonude <> 1;
    END

END
GO
