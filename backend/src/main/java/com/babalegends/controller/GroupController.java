package com.babalegends.controller;

import com.babalegends.dto.AddMemberRequestDto;
import com.babalegends.dto.ChangeRoleRequestDto;
import com.babalegends.dto.CreateGroupRequestDto;
import com.babalegends.dto.GroupResponseDto;
import com.babalegends.dto.MemberResponseDto;
import com.babalegends.dto.UpdateGroupRequestDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Grupos", description = "Gerenciamento de grupos/peladas e controle de membros e papéis")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Listar meus grupos", description = "Retorna todos os grupos dos quais o usuário autenticado é membro.")
    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(groupService.listMyGroups(principal.getId()));
    }

    @Operation(summary = "Buscar grupo por ID", description = "Retorna os detalhes de um grupo específico.")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroup(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId, principal.getId()));
    }

    @Operation(summary = "Listar membros do grupo", description = "Retorna a lista de todos os membros e seus papéis no grupo informado.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<MemberResponseDto>> listMembers(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.listMyGroupMembers(principal.getId(), groupId));
    }

    @Operation(summary = "Criar novo grupo", description = "Cria um novo grupo e define o usuário autenticado como administrador.")
    @PostMapping
    public ResponseEntity<GroupResponseDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody @Valid CreateGroupRequestDto createGroupRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(principal.getId(), createGroupRequestDto));
    }

    @Operation(summary = "Atualizar grupo", description = "Atualiza nome ou permissões do grupo (exclusivo para administradores).")
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> update(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID groupId,
                                                   @RequestBody @Valid UpdateGroupRequestDto updateGroupRequestDto) {
        return ResponseEntity.ok(groupService.updateGroup(principal.getId(), groupId, updateGroupRequestDto));
    }

    @Operation(summary = "Adicionar membro ao grupo", description = "Adiciona um usuário ao grupo através do seu endereço de e-mail.")
    @PostMapping("/{groupId}/members")
    public ResponseEntity<MemberResponseDto> addMember(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable UUID groupId,
                                                       @RequestBody @Valid AddMemberRequestDto addMemberRequestDto) {
        return ResponseEntity.ok(groupService.addMember(principal.getId(), groupId, addMemberRequestDto));
    }

    @Operation(summary = "Alterar papel do membro", description = "Altera o papel de um membro do grupo (ex: ADMIN para MEMBER ou vice-versa).")
    @PatchMapping("/{groupId}/members/{userId}")
    public ResponseEntity<MemberResponseDto> changeUserRole(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable UUID groupId, @PathVariable UUID userId,
                                                            @RequestBody @Valid ChangeRoleRequestDto changeRoleRequestDto) {
        return ResponseEntity.ok(groupService.changeMemberRole(
                principal.getId(), groupId, userId, changeRoleRequestDto.role())
        );
    }

    @Operation(summary = "Remover membro do grupo", description = "Remove um membro do grupo (ou permite que o membro saia do grupo).")
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable UUID groupId, @PathVariable UUID userId) {
        groupService.removeMember(principal.getId(), groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
