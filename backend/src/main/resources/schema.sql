CREATE TABLE IF NOT EXISTS transaction_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(20) NOT NULL COMMENT 'income 或 expense',
  category VARCHAR(50) NOT NULL COMMENT '分类',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  payment_method VARCHAR(50) NULL COMMENT '支付方式',
  transaction_date DATE NOT NULL COMMENT '交易日期',
  note VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_transaction_date (transaction_date),
  INDEX idx_type_date (type, transaction_date),
  INDEX idx_category (category),
  INDEX idx_payment_method (payment_method)
);
