-- Add payment QR code columns to the seller table
ALTER TABLE seller 
ADD COLUMN wechat_qr_code VARCHAR(200) COMMENT 'WeChat Pay QR Code image path',
ADD COLUMN alipay_qr_code VARCHAR(200) COMMENT 'Alipay QR Code image path'; 