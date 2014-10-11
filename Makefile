deploy_production:
	lein do clean, uberjar
	ansible-playbook -i hosts -u yambox -K infrastructure/deploy.yml

setup_production:
	ansible-playbook -i hosts -u yambox -K infrastructure/setup.yml
