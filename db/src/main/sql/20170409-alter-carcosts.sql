ALTER TABLE carcosts
MODIFY car_cost_comment TEXT;

ALTER TABLE carcosts
ADD column car_cost_car_admin_comment TEXT,
ADD column car_cost_car_owner_comment TEXT;
