package itmo.highload.service

import itmo.highload.dto.response.BalanceResponse
import itmo.highload.dto.response.PurposeResponse
import itmo.highload.model.Balance
import itmo.highload.repository.BalanceRepository
import itmo.highload.service.mapper.BalanceMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BalanceService(private val balanceRepository: BalanceRepository) {

    fun getById(id: Int): BalanceResponse {
        return BalanceMapper.toBalanceResponse(balanceRepository.findById(id).get())
    }

    fun getAll(pageable: Pageable): Page<BalanceResponse> {
        val page: Page<Balance> = balanceRepository.findAll(pageable)
        return page.map { balance -> BalanceMapper.toBalanceResponse(balance) }
    }

    fun getAllPurposes(pageable: Pageable): Page<PurposeResponse> {
        val page: Page<Balance> = balanceRepository.findAll(pageable)
        return page.map { balance -> BalanceMapper.toPurposeResponse(balance) }
    }

    fun addPurpose(name: String): PurposeResponse {
        return BalanceMapper.toPurposeResponse(balanceRepository.save(BalanceMapper.toEntity(name)))
    }


}
