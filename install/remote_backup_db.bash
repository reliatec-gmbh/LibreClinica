#!/bin/bash
#Escrito por Dennys Ordoñez, y adaptado por Lucio Montero (2024).
pushd .
cd ~/Alagant_projects/LibreClinica
docker compose exec postgres pg_dump -U clinica -h postgres -d libreclinica << EOF
clinica
EOF
popd
