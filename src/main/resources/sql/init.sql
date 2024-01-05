CREATE if not exist TABLE currencies(
    id INTEGER PRIMARY KEY autoincrement,
    code TEXT not null unique,
    fullname TEXT not null,
    sign TEXT not null
);

create if not exist table exchange_rate(
	id INTEGER primary key autoincrement,
	base_currency_id integer not null,
	target_currency_id integer not null,
	rate real not null,
	unique(base_currency_id, target_currency_id),
	foreign key(base_currency_id) references currencies(id),
	foreign key(target_currency_id) references currencies(id)
);