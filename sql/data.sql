-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.11.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for party_rental
CREATE DATABASE IF NOT EXISTS `party_rental` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `party_rental`;

-- Dumping structure for table party_rental.customer_type
CREATE TABLE IF NOT EXISTS `customer_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(20) NOT NULL,
  `tax_description` varchar(5) NOT NULL,
  `tax` float NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `label` (`label`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table party_rental.customer_type: ~5 rows (approximately)
/*!40000 ALTER TABLE `customer_type` DISABLE KEYS */;
INSERT INTO `customer_type` (`id`, `label`, `tax_description`, `tax`) VALUES
	(1, 'DOMESTIC', 'GST', 8),
	(2, 'GOVERNMENT', 'GST', 8),
	(3, 'DIPLOMATIC', 'GST', 8),
	(4, 'PRIVATE', 'GST', 8),
	(5, 'RESORTS', 'TGST', 16);
/*!40000 ALTER TABLE `customer_type` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
